package com.objectcomputing.checkins.services.agenda_item;

import com.objectcomputing.checkins.services.checkins.CheckIn;
import com.objectcomputing.checkins.services.checkins.CheckInBadArgException;
import com.objectcomputing.checkins.services.checkins.CheckInRepository;
import com.objectcomputing.checkins.services.memberprofile.MemberProfile;
import com.objectcomputing.checkins.services.memberprofile.MemberProfileRepository;
import com.objectcomputing.checkins.services.memberprofile.currentuser.CurrentUserServices;
import com.objectcomputing.checkins.services.role.RoleType;
import io.micronaut.security.utils.SecurityService;
import jnr.a64asm.Mem;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Singleton
public class AgendaItemServicesImpl implements AgendaItemServices {

    private CheckInRepository checkinRepo;
    private AgendaItemRepository agendaItemRepository;
    private MemberProfileRepository memberRepo;
    private SecurityService securityService;
    private CurrentUserServices currentUserServices;

    public AgendaItemServicesImpl(CheckInRepository checkinRepo, AgendaItemRepository agendaItemRepository,
                                   MemberProfileRepository memberRepo, SecurityService securityService,
                                   CurrentUserServices currentUserServices) {
        this.checkinRepo = checkinRepo;
        this.agendaItemRepository = agendaItemRepository;
        this.memberRepo = memberRepo;
        this.securityService = securityService;
        this.currentUserServices = currentUserServices;
    }


    @Override
    public AgendaItem save(AgendaItem agendaItem) {
        AgendaItem agendaItemRet = null;
        String workEmail = securityService!=null ? securityService.getAuthentication().get().getAttributes().get("email").toString() : null;
        MemberProfile currentUser = workEmail!=null? currentUserServices.findOrSaveUser(null, workEmail) : null;
        Boolean isAdmin = securityService!=null ? securityService.hasRole(RoleType.Constants.ADMIN_ROLE) : false;

        if (agendaItem != null) {
            final UUID checkinId = agendaItem.getCheckinid();
            final UUID createById = agendaItem.getCreatedbyid();
            CheckIn checkinRecord = checkinRepo.findById(checkinId).orElse(null);
            Boolean isCompleted  = checkinRecord!=null?checkinRecord.isCompleted():null;
            final UUID pdlId = checkinRecord!=null?checkinRecord.getPdlId():null;
            if (checkinId == null || createById == null) {
                throw new AgendaItemBadArgException(String.format("Invalid agenda item %s", agendaItem));
            } else if (agendaItem.getId() != null) {
                throw new AgendaItemBadArgException(String.format("Found unexpected id %s for agenda item", agendaItem.getId()));
            } else if (checkinRepo.findById(checkinId).isEmpty()) {
                throw new AgendaItemBadArgException(String.format("CheckIn %s doesn't exist", checkinId));
            } else if (memberRepo.findById(createById).isEmpty()) {
                throw new AgendaItemBadArgException(String.format("Member %s doesn't exist", createById));
            } else if(!isAdmin&&!isCompleted) {
                if(!currentUser.getId().equals(pdlId)&&!currentUser.getId().equals(createById)) {
                    throw new AgendaItemBadArgException(String.format("Member %s is unauthorized to do this operation", currentUser.getId()));
                }
            }

            agendaItemRet = agendaItemRepository.save(agendaItem);
        }
        return agendaItemRet;
    }

    @Override
    public AgendaItem read(@NotNull UUID id) {
        String workEmail = securityService!=null ? securityService.getAuthentication().get().getAttributes().get("email").toString() : null;
        MemberProfile currentUser = workEmail!=null? currentUserServices.findOrSaveUser(null, workEmail) : null;
        Boolean isAdmin = securityService!=null ? securityService.hasRole(RoleType.Constants.ADMIN_ROLE) : false;
        AgendaItem agendaItemResult =  agendaItemRepository.findById(id).orElse(null);

        if(agendaItemResult == null) {
            throw new AgendaItemBadArgException(String.format("Invalid agenda item id %s",id));
        } else if(!isAdmin) {
            CheckIn checkinRecord = checkinRepo.findById(agendaItemResult.getCheckinid()).orElse(null);
            final UUID pdlId = checkinRecord!=null?checkinRecord.getPdlId():null;
            final UUID createById = checkinRecord!=null?checkinRecord.getTeamMemberId():null;
            if(!currentUser.getId().equals(pdlId)&&!currentUser.getId().equals(createById)){
                throw new AgendaItemBadArgException(String.format("Member %s is unauthorized to do this operation", currentUser.getId()));
            }
        }
        return agendaItemResult;
    }


    @Override
    public AgendaItem update(AgendaItem agendaItem) {
        AgendaItem agendaItemRet = null;
        String workEmail = securityService!=null ? securityService.getAuthentication().get().getAttributes().get("email").toString() : null;
        MemberProfile currentUser = workEmail!=null? currentUserServices.findOrSaveUser(null, workEmail) : null;
        Boolean isAdmin = securityService!=null ? securityService.hasRole(RoleType.Constants.ADMIN_ROLE) : false;

        if (agendaItem != null) {
            final UUID id = agendaItem.getId();
            final UUID checkinId = agendaItem.getCheckinid();
            final UUID createById = agendaItem.getCreatedbyid();
            CheckIn checkinRecord = checkinRepo.findById(checkinId).orElse(null);
            Boolean isCompleted  = checkinRecord!=null ?checkinRecord.isCompleted():null;
            final UUID pdlId = checkinRecord!=null?checkinRecord.getPdlId():null;

            if (checkinId == null || createById == null) {
                throw new AgendaItemBadArgException(String.format("Invalid agenda item %s", agendaItem));
            } else if (id == null || agendaItemRepository.findById(id).isEmpty()) {
                throw new AgendaItemBadArgException(String.format("Unable to locate agenda item to update with id %s", agendaItem.getId()));
            } else if (checkinRepo.findById(checkinId).isEmpty()) {
                throw new AgendaItemBadArgException(String.format("CheckIn %s doesn't exist", checkinId));
            } else if (memberRepo.findById(createById).isEmpty()) {
                throw new AgendaItemBadArgException(String.format("Member %s doesn't exist", createById));
            } else if(!isAdmin&&!isCompleted) {
                if(!currentUser.getId().equals(pdlId)&&!currentUser.getId().equals(createById)) {
                    throw new AgendaItemBadArgException(String.format("Member %s is unauthorized to do this operation", currentUser.getId()));
                }
            }

            agendaItemRet = agendaItemRepository.update(agendaItem);
        }
        return agendaItemRet;
    }


    @Override
    public Set<AgendaItem> findByFields(UUID checkinid, UUID createbyid) {
        Set<AgendaItem> agendaItem = new HashSet<>();
        String workEmail = securityService!=null ? securityService.getAuthentication().get().getAttributes().get("email").toString() : null;
        MemberProfile currentUser = workEmail!=null? currentUserServices.findOrSaveUser(null, workEmail) : null;
        Boolean isAdmin = securityService!=null ? securityService.hasRole(RoleType.Constants.ADMIN_ROLE) : false;

        agendaItemRepository.findAll().forEach(agendaItem::add);

            if(checkinid!=null) {
                CheckIn checkinRecord = checkinRepo.findById(checkinid).orElse(null);
                final UUID pdlId = checkinRecord!=null?checkinRecord.getPdlId():null;
                final UUID teamMemberId = checkinRecord!=null?checkinRecord.getTeamMemberId():null;
                if(!currentUser.getId().equals(pdlId)&&!currentUser.getId().equals(teamMemberId)&&!isAdmin){
                    throw new AgendaItemBadArgException(String.format("Member %s is unauthorized to do this operation", currentUser.getId()));
                } else {
                    agendaItem.retainAll(agendaItemRepository.findByCheckinid(checkinid));
                }
            } else if(createbyid!=null) {
                MemberProfile memberRecord = memberRepo.findById(createbyid).orElse(null);

                if(!currentUser.getId().equals(memberRecord.getId())&&!isAdmin){
                    throw new AgendaItemBadArgException(String.format("Member %s is unauthorized to do this operation", currentUser.getId()));
                } else {
                    agendaItem.retainAll(agendaItemRepository.findByCreatedbyid(createbyid));
                }
            } else if(!isAdmin) {
                throw new AgendaItemBadArgException(String.format("Member %s is unauthorized to do this operation", currentUser.getId()));
            }
        return agendaItem;
    }

    @Override
    public void delete(@NotNull UUID id) {
        String workEmail = securityService!=null ? securityService.getAuthentication().get().getAttributes().get("email").toString() : null;
        MemberProfile currentUser = workEmail!=null? currentUserServices.findOrSaveUser(null, workEmail) : null;
        Boolean isAdmin = securityService!=null ? securityService.hasRole(RoleType.Constants.ADMIN_ROLE) : false;
        AgendaItem agendaItemResult =  agendaItemRepository.findById(id).orElse(null);

        if(agendaItemResult == null) {
            throw new AgendaItemBadArgException(String.format("Invalid agenda item id %s",id));
        } else if(!isAdmin) {
            CheckIn checkinRecord = checkinRepo.findById(agendaItemResult.getCheckinid()).orElse(null);
            final UUID pdlId = checkinRecord!=null?checkinRecord.getPdlId():null;
            final UUID createById = checkinRecord!=null?checkinRecord.getTeamMemberId():null;
            if(!currentUser.getId().equals(pdlId)&&!currentUser.getId().equals(createById)){
                throw new AgendaItemBadArgException(String.format("Member %s is unauthorized to do this operation", currentUser.getId()));
            }
        }
        agendaItemRepository.deleteById(id);
    }
}