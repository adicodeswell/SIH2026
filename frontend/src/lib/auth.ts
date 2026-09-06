import Keycloak from 'keycloak-js';

// Configuration matches the docker-compose Keycloak setup
const keycloakConfig = {
  url: 'http://localhost:8080',
  realm: 'mahasetu',
  clientId: 'frontend-client', // Assuming a standard public client ID for the React app
};

const keycloak = new Keycloak(keycloakConfig);

export default keycloak;
