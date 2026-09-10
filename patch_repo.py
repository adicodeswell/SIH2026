with open('./backend/application-service/src/main/java/com/mahasetu/application/repository/ApplicationRepository.java', 'r') as f:
    content = f.read()
    
content = content.replace("Optional<Application> findByApplicationNumber(String applicationNumber);", "Optional<Application> findByApplicationNumber(String applicationNumber);\n    java.util.List<Application> findByCitizen_CitizenIdOrderByCreatedAtDesc(String citizenId);")

with open('./backend/application-service/src/main/java/com/mahasetu/application/repository/ApplicationRepository.java', 'w') as f:
    f.write(content)
