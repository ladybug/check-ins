package com.objectcomputing.checkins.services.fixture;

import com.objectcomputing.checkins.services.memberprofile.MemberProfile;
import com.objectcomputing.checkins.services.team.Team;
import com.objectcomputing.checkins.services.team.TeamUpdateDTO;
import com.objectcomputing.checkins.services.team.member.TeamMember;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public interface TeamFixture extends RepositoryFixture{
    default Team createDefultTeam() {
        return getTeamRepository().save(new Team(UUID.randomUUID(),"Ninja","Warriors"));
    }

    default TeamUpdateDTO makeDefaultTeamUpdateDTO(UUID fromUUID, List<MemberProfile> members) {
        TeamUpdateDTO newDTO = new TeamUpdateDTO();
        newDTO.setId(fromUUID);
        newDTO.setName("different name");
        newDTO.setDescription("different description");
        newDTO.setTeamMembers(new ArrayList<>());
        for (MemberProfile member : members) {
            TeamMember newTeamMember = new TeamMember(fromUUID, member.getId(), false);
            newDTO.getTeamMembers().add(newTeamMember);
        }
        newDTO.getTeamMembers().get(0).setLead(true);
        return newDTO;
    }

    default Team createAnotherDefultTeam() {
        return getTeamRepository().save(new Team(UUID.randomUUID(),"Coding","Warriors"));
    }
}
