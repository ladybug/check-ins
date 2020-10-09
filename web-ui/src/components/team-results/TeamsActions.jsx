import React, { useState } from 'react';
import Container from '@material-ui/core/Container';
import { Button } from '@material-ui/core';
import GroupIcon from '@material-ui/icons/Group';
import AddTeamModal from './EditTeamModal';
import './TeamResults.css'

const displayName = "TeamsActions";

const TeamsActions = () => {
    const [open, setOpen] = useState(false);

    const handleOpen = () => setOpen(true);

    const handleClose = () => setOpen(false);

    return (
        <Container maxWidth="md">
            <div className="team-actions">
                <Button startIcon={<GroupIcon />} onClick={handleOpen}>
                    Add Team
                </Button>
                <AddTeamModal open={open} onClose={handleClose} onSave={(team) => { console.log(JSON.stringify(team)); handleClose(); }} />
            </div>
        </Container>
    )
};

TeamsActions.displayName = displayName;

export default TeamsActions;
