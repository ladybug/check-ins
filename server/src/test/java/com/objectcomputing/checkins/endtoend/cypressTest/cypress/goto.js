export function goToSignIn() {
    window.sessionStorage.clear();
    cy.viewport('ipad-2');
    cy.visit('/');
  }
  
  export function goToHosting() {
    goToSignIn();
    cy.get('input[type=email]').type('mark@gmail.com');
    cy.get(':nth-child(3) > input').type('Mark Volkmann');
    cy.get(':nth-child(4) > input').type('OCI');
    cy.get('.submit').click();
  }
  
  export function goToEngineering() {
    goToHosting();
    cy.contains('Next').click();
  }
  
  export function goToSourcing() {
    goToEngineering();
    cy.contains('Next').click();
  }
  
  export function goToPractices() {
    goToSourcing();
    cy.contains('Next').click();
  }
  
  export function goToSummary() {
    goToPractices();
    cy.contains('Next').click();
  }
  