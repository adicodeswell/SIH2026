import json

with open('./infrastructure/keycloak/realm-export.json', 'r') as f:
    data = json.load(f)

for client in data.get('clients', []):
    if client.get('clientId') == 'frontend-portal':
        client['protocolMappers'] = client.get('protocolMappers', [])
        client['protocolMappers'].append({
            "name": "department-mapper",
            "protocol": "openid-connect",
            "protocolMapper": "oidc-usermodel-attribute-mapper",
            "consentRequired": False,
            "config": {
                "user.attribute": "department",
                "claim.name": "department",
                "jsonType.label": "String",
                "id.token.claim": "true",
                "access.token.claim": "true",
                "userinfo.token.claim": "true"
            }
        })

for user in data.get('users', []):
    if user.get('username') == 'officer_123':
        user['attributes'] = user.get('attributes', {})
        user['attributes']['department'] = ["DEPT-SKILLS"]

with open('./infrastructure/keycloak/realm-export.json', 'w') as f:
    json.dump(data, f, indent=2)

print("Updated realm-export.json")
