export function goToHomePage() {
    window.sessionStorage.clear();
    cy.visit('https://checkins.objectcomputing.com/upload');
}

export function goToHostingPage() {
    goToSignInPage();
    cy.get('input[type=email]').type('abc@gmail.com');
    cy.get(':nth-child(3) > input').type('RESHMA TAJ');
    cy.get(':nth-child(4) > input').type('OCI');
    cy.get('.submit').click();
  }

  export function goToEngineeringPage() {
    goToHostingPage()
    windowScrollDown()
    skip()
  }

  export function goToSourcingPage() {
    goToEngineeringPage()
    windowScrollDown()
    skip()
  }

  export function goToPracticesPage() {
    goToSourcingPage()
    windowScrollDown()
    skip()
  }

  export function goToAssesmentPage() {
    goToPracticesPage()
    windowScrollDown()
    nextButton()
  }

  export function windowScrollDown() {
    cy.get('.top').scrollTo('bottom')
  }

  export function currentStateLabels() {
    cy.get('.current-state > header').contains('Current State').should('be.visible')
      cy.get(':nth-child(2) > .title').contains('Cost/Maintainability').should('be.visible')
      cy.get(':nth-child(3) > .title').contains('Functional Range/Future Proof').should('be.visible')
      cy.get(':nth-child(4) > .title').contains('Availability').should('be.visible')
      cy.get(':nth-child(5) > .title').contains('Security').should('be.visible')
      cy.get(':nth-child(6) > .title').contains('Innovation/Time to Market').should('be.visible')
  }

  export function nextButton() {
    cy.get('.next').click()
  }

  export function alertBox() {
    cy.get('dialog').contains('Percentages must add to 100')
    cy.contains('Close').click()
  }

  export function skip() {
    cy.get('.skip').click()
  }

  export function back() {
    cy.get('.back').click()
  }
