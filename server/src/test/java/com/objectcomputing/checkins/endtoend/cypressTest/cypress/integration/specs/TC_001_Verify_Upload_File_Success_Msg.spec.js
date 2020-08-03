import {
  goToHomePage
  } from '../../functions'; 


describe('CheckIn app', () => {
    it('Home page', () => {
      goToHomePage();
    })
})

describe('Verify success msg after uploading a file', () => {
    it('Upload a File', () => {
      const filePath = 'UploadFile.txt';
      cy.get("#file").attachFile(filePath);
    })
    it('Click on upload button', () => {
      cy.get('button').click();
      cy.wait(3500)
    })
    it('Verify success Msg', () => {
      cy.get('.success').should('contain', 'The file UploadFile.txt was uploaded')
    })
})