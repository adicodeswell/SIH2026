import xml.etree.ElementTree as ET

tree = ET.parse('backend/security-workflow-service/src/main/resources/bpmn/application-orchestration.bpmn')
root = tree.getroot()

namespace = {'bpmn': 'http://www.omg.org/spec/BPMN/20100524/MODEL'}
ET.register_namespace('bpmn', namespace['bpmn'])
ET.register_namespace('camunda', 'http://camunda.org/schema/1.0/bpmn')

process = root.find('bpmn:process', namespace)

# Find Task_FetchInterop and Flow_Interop
task_interop = process.find(".//*[@id='Task_FetchInterop']", namespace)
flow_interop = process.find(".//*[@id='Flow_Interop']", namespace)

# Change Flow_Interop target to Task_VerifyData
flow_interop.set('targetRef', 'Task_VerifyData')

# Create Task_VerifyData
task_verify = ET.Element('{http://www.omg.org/spec/BPMN/20100524/MODEL}serviceTask', {
    'id': 'Task_VerifyData',
    'name': 'Verify Data',
    '{http://camunda.org/schema/1.0/bpmn}delegateExpression': '${verifyDataWorker}'
})

incoming = ET.SubElement(task_verify, '{http://www.omg.org/spec/BPMN/20100524/MODEL}incoming')
incoming.text = 'Flow_Interop'
outgoing = ET.SubElement(task_verify, '{http://www.omg.org/spec/BPMN/20100524/MODEL}outgoing')
outgoing.text = 'Flow_Verify'

# Create Flow_Verify
flow_verify = ET.Element('{http://www.omg.org/spec/BPMN/20100524/MODEL}sequenceFlow', {
    'id': 'Flow_VerifyData_To_PendingReview',
    'sourceRef': 'Task_VerifyData',
    'targetRef': 'Task_SetPendingReview'
})

# Update Task_SetPendingReview
task_pending = process.find(".//*[@id='Task_SetPendingReview']", namespace)
for inc in task_pending.findall('bpmn:incoming', namespace):
    if inc.text == 'Flow_Interop':
        inc.text = 'Flow_VerifyData_To_PendingReview'

# Insert the new task and flow right after Flow_Interop
idx = list(process).index(flow_interop)
process.insert(idx + 1, task_verify)
process.insert(idx + 2, flow_verify)

tree.write('backend/security-workflow-service/src/main/resources/bpmn/application-orchestration.bpmn', xml_declaration=True, encoding='UTF-8')
