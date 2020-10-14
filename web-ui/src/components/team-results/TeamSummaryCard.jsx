import React, { useContext, useState } from 'react';
import PropTypes from 'prop-types';
import { Button, Card, CardActions, CardContent, CardHeader } from '@material-ui/core';
import { Skeleton } from '@material-ui/lab';
import { AppContext } from '../../context/AppContext';
import EditTeamModal from './EditTeamModal';
import { updateTeam,} from '../../api/team';

const propTypes = {
    team: PropTypes.shape({
        id: PropTypes.string,
        name: PropTypes.string,
        description: PropTypes.string
    })
}

const displayName = "TeamSummaryCard";

const TeamSummaryCard = ({ team }) => {

    const {state} = useContext(AppContext);

    const { userProfile } = state;

    let teamMembers = AppContext.selectMemberProfilesByTeamId(state)(team.id);


    let leads = teamMembers == null ? null : teamMembers.filter((teamMember) => teamMember.lead);
    let nonLeads = teamMembers == null ? null : teamMembers.filter((teamMember) => !teamMember.lead);

    team.teamLeads = leads;
    team.teamMembers = nonLeads;

    const [open, setOpen] = useState(false);

    const handleOpen = () => setOpen(true);

    const formatMember = (member, isLead) => {
        if (!member.memberid) {
            member.memberid = member.id;
        }
        member.lead = isLead;
        return member;
    };

    const handleClose = (alteredTeam) => {
        setOpen(false);
        let postBody = {
            name: alteredTeam.name,
            description: alteredTeam.description,
            teamMembers: [...alteredTeam.teamMembers.map((member) => formatMember(member, false)),
                ...alteredTeam.teamLeads.map((lead) => formatMember(lead, true))],
            id: alteredTeam.id,
        }
        alteredTeam.teamMembers = [...alteredTeam.teamMembers, ...alteredTeam.teamLeads];
        updateTeam(postBody);
        team = alteredTeam;
        teamMembers = AppContext.selectMemberProfilesByTeamId(state)(team.id);
        leads = teamMembers == null ? null : teamMembers.filter((teamMember) => teamMember.lead);
        nonLeads = teamMembers == null ? null : teamMembers.filter((teamMember) => !teamMember.lead);
        team.teamLeads = leads;
        team.teamMembers = nonLeads;

    };

    const userCanEdit = () => {
        const leads = teamMembers.filter((teamMember) => teamMember.lead);
        const thisUserLead = leads.filter((lead) => lead.memberid === userProfile.memberProfile.id);
        const isLead = thisUserLead.length > 0;
        if (userProfile.role.includes("ADMIN") || isLead) {
            return true;
        }

        return false;
    };

    return (
        <Card>
            <CardHeader title={team.name} subheader={team.description} />
            <CardContent>
                {
                    teamMembers == null ?
                        <React.Fragment>
                            <Skeleton />
                            <Skeleton />
                        </React.Fragment> :
                        <React.Fragment>
                            <strong>Team Leads: </strong>
                            {
                                leads.map((lead, index) => {
                                    return index !== leads.length - 1 ? `${lead.name}, ` : lead.name
                                })
                            }
                            <br />
                            <strong>Team Members: </strong>
                            {
                                nonLeads.map((member, index) => {
                                    return index !== nonLeads.length - 1 ? `${member.name}, ` : member.name
                                })
                            }
                        </React.Fragment>
                }
            </CardContent>
            <CardActions style={{display: userCanEdit() ? "block" : "none" }}>
                <Button onClick={handleOpen}>Edit Team</Button>
                <Button>Delete Team</Button>
            </CardActions>
            <EditTeamModal team={team} open={open} onClose={handleClose} onSave={(alteredTeam) => { handleClose(alteredTeam); }} />
        </Card>
    );
};

TeamSummaryCard.displayName = displayName;
TeamSummaryCard.propTypes = propTypes;

export default TeamSummaryCard;
