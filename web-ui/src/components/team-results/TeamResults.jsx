import React, { useEffect, useContext, useState } from 'react';
import PropTypes from 'prop-types';
import Container from '@material-ui/core/Container';
import TeamSummaryCard from './TeamSummaryCard';
import { AppContext } from '../../context/AppContext';
import { getAllTeams } from '../../api/team';
import './TeamResults.css'

const propTypes = {
    teams: PropTypes.arrayOf(PropTypes.shape({
        id: PropTypes.string,
        name: PropTypes.string,
        description: PropTypes.string
    }))
};

const displayName = "TeamResults";

const TeamResults = () => {
    const { state } = useContext(AppContext);
    const [ teams, setTeams ] = useState([]);

    useEffect(() => {
        console.log("using effect");
        async function getTeams() {
            let res = await getAllTeams();
            let data =
                res.payload &&
                res.payload.data &&
                res.payload.status === 200 &&
                !res.error
                ? res.payload.data
                : null;
            if (data) {
                //dispatch({ type: UPDATE_TEAMS, payload: data });
                console.log(data);
                setTeams(data);
            }
        };
        getTeams();
    }, [setTeams]);

    const updateTeam = (updatedTeam) => {
        console.log("updating");
        console.log(updatedTeam);
        for (var i = 0; i < teams.length; i++) {
            if (teams[i].id === updatedTeam.id) {
                teams[i] = updatedTeam;
            }
        }
        console.log(teams);
        setTeams(teams);
    };

    return (
        <Container maxWidth="md">
            {teams.map((team) => (
                <TeamSummaryCard key={`team-summary-${team.id}`} team={team} handleUpdate={updateTeam} />
            ))}
        </Container>
    )
};

TeamResults.propTypes = propTypes;
TeamResults.displayName = displayName;

export default TeamResults;
