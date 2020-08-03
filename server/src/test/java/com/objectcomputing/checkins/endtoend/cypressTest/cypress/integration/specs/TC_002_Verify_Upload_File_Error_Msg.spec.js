import {
  goToHomePage
  } from '../../functions';


describe('CheckIn app', () => {
    it('Home page', () => {
      goToHomePage();
    })
})

describe('Verify error msg when upload btn is clicked without uploading a file', () => {
    it('Click on upload button', () => {
      cy.get('button').click();
    })
    it('Verify error Msg', () => {
      cy.get('.error').should('contain', 'Please select a file before uploading.')
    })
})