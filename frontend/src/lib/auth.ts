import Keycloak from 'keycloak-js';

// Configuration matches the docker-compose Keycloak setup
const keycloakConfig = {
  url: 'http://localhost:8080',
  realm: 'mahasetu',
  clientId: 'frontend-portal', // Matches the realm-export.json client ID
};

const keycloak = new Keycloak(keycloakConfig);

export default keycloak;
