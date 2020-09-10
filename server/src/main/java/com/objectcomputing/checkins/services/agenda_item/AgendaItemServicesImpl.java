package com.objectcomputing.checkins.services.agenda_item;

import com.objectcomputing.checkins.services.checkins.CheckInRepository;
import com.objectcomputing.checkins.services.memberprofile.MemberProfileRepository;

import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import com.objectcomputing.checkins.services.memberprofile.MemberProfile;
import com.objectcomputing.checkins.services.memberprofile.currentuser.CurrentUserServices;
import com.objectcomputing.checkins.services.role.RoleType;
import io.micronaut.security.utils.SecurityService;
import javax.inject.Singleton;

@Singleton
public class AgendaItemServicesImpl implements AgendaItemServices {

    private CheckInRepository checkinRepo;
    private AgendaItemRepository agendaItemRepo;
    private MemberProfileRepository memberRepo;
    private SecurityService securityService;
    private CurrentUserServices currentUserServices;

    public AgendaItemServicesImpl(AgendaItemRepository agendaItemRepo, CheckInRepository checkinRepo, MemberProfileRepository memberRepo, SecurityService securityService, CurrentUserServices currentUserServices) {
        this.agendaItemRepo = agendaItemRepo;
        this.checkinRepo = checkinRepo;
        this.memberRepo = memberRepo;
        this.securityService = securityService;
        this.currentUserServices = currentUserServices;
    }

    @Override
    public AgendaItem save(@NotNull AgendaItem agendaItem) {

        final UUID createById = agendaItem.getCreatedbyid();
        final UUID checkinId = agendaItem.getCheckinid();

        String workEmail = securityService!=null ? securityService.getAuthentication().get().getAttributes().get("email").toString() : null;
        MemberProfile currentUser = workEmail!=null? currentUserServices.findOrSaveUser(null, workEmail) : null;
        Boolean isAdmin = securityService!=null ? securityService.hasRole(RoleType.Constants.ADMIN_ROLE) : false;

        if(agendaItem.getId()!=null) {
            throw new AgendaItemBadArgException(String.format("Found unexpected id for agendaItem  %s", agendaItem.getId()));
        } else if (checkinId == null || createById == null) {
            throw new AgendaItemBadArgException(String.format("Invalid agendaItem %s", agendaItem));
        } else if(memberRepo.findById(createById).isEmpty()) {
            throw new AgendaItemBadArgException(String.format("Member %s doesn't exist", createById));
        } else if (!checkinRepo.findById(checkinId).isPresent()) {
            throw new AgendaItemBadArgException(String.format("CheckIn %s doesn't exist", checkinId));
        } else if(!isAdmin) {
            if(!currentUser.getId().equals(agendaItem.getCreatedbyid())) {
                throw new AgendaItemBadArgException(String.format("Member %s is unauthorized to do this operation", currentUser.getId()));
            }
        }

        return agendaItemRepo.save(agendaItem);
    }

    @Override
    public AgendaItem read(@NotNull UUID id) {

        String workEmail = securityService!=null ? securityService.getAuthentication().get().getAttributes().get("email").toString() : null;
        MemberProfile currentUser = workEmail!=null? currentUserServices.findOrSaveUser(null, workEmail) : null;
        Boolean isAdmin = securityService!=null ? securityService.hasRole(RoleType.Constants.ADMIN_ROLE) : false;

        AgendaItem result = agendaItemRepo.findById(id).orElse(null);

        if (result == null) {
            throw new AgendaItemBadArgException(String.format("Invalid agendaItem id %s", id));
        } else if(!isAdmin) {
            if(!currentUser.getId().equals(result.getCreatedbyid())) {
                throw new AgendaItemBadArgException(String.format("Member %s is unauthorized to do this operation", currentUser.getId()));
            }
        }

        return result;
    }

    @Override
    public AgendaItem update(@NotNull AgendaItem agendaItem) {

        final UUID id = agendaItem.getId();
        final UUID createById = agendaItem.getCreatedbyid();
        final UUID checkinId = agendaItem.getCheckinid();
        // LocalDateTime chkInDate = checkIn.getCheckInDate();

        String workEmail = securityService!=null ? securityService.getAuthentication().get().getAttributes().get("email").toString() : null;
        MemberProfile currentUser = workEmail!=null? currentUserServices.findOrSaveUser(null, workEmail) : null;
        Boolean isAdmin = securityService!=null ? securityService.hasRole(RoleType.Constants.ADMIN_ROLE) : false;

        if(id==null||!agendaItemRepo.findById(id).isPresent()) {
            throw new AgendaItemBadArgException(String.format("Unable to find agendaItem record with id %s", agendaItem.getId()));
        } else if(checkinId == null || createById == null) {
            throw new AgendaItemBadArgException(String.format("Invalid agendaItem %s", agendaItem));
        } else if(!memberRepo.findById(createById).isPresent()) {
            throw new AgendaItemBadArgException(String.format("Member %s doesn't exist", createById));
        } else if (!checkinRepo.findById(checkinId).isPresent()) {
            throw new AgendaItemBadArgException(String.format("CheckIn %s doesn't exist", checkinId));
        } else if(!isAdmin) {
            if(!currentUser.getId().equals(agendaItem.getCreatedbyid())) {
                throw new AgendaItemBadArgException(String.format("Member %s is unauthorized to do this operation", currentUser.getId()));
            // } else if(checkinRepo.findById(id).get().isCompleted()) {
            //     throw new AgendaItemBadArgException(String.format("Checkin with id %s is complete and cannot be updated", agendaItem.getId()));
            }
        }

        return agendaItemRepo.update(agendaItem);
    }

    @Override
    public Set<AgendaItem> findByFields(UUID checkinid, UUID createdbyid) {
        Set<AgendaItem> agendaItems = new HashSet<>();
        String workEmail = securityService!=null ? securityService.getAuthentication().get().getAttributes().get("email").toString() : null;
        MemberProfile currentUser = workEmail!=null? currentUserServices.findOrSaveUser(null, workEmail) : null;
        Boolean isAdmin = securityService!=null ? securityService.hasRole(RoleType.Constants.ADMIN_ROLE) : false;

        if(isAdmin || currentUser.getId().equals(createdbyid) ||
                currentUser.getId().equals(memberRepo.findById(createdbyid).get().getPdlId())) {

            agendaItemRepo.findAll().forEach(agendaItems::add);
            if (createdbyid != null) {
                agendaItems.retainAll(agendaItemRepo.findByCheckinid(checkinid));
            } else if (checkinid != null) {
                agendaItems.retainAll(agendaItemRepo.findByCreatedbyid(createdbyid));
            // } else if (completed != null) {
            //     agendaItems.retainAll(checkinRepo.findByCompleted(completed));
            }
            return agendaItems;
        }

        throw new AgendaItemBadArgException(String.format("Member %s is unauthorized to do this operation", currentUser.getId()));
    }

    public void delete(@NotNull UUID id) {
        String workEmail = securityService!=null ? securityService.getAuthentication().get().getAttributes().get("email").toString() : null;
        MemberProfile currentUser = workEmail!=null? currentUserServices.findOrSaveUser(null, workEmail) : null;
        Boolean isAdmin = securityService!=null ? securityService.hasRole(RoleType.Constants.ADMIN_ROLE) : false;

        AgendaItem result = agendaItemRepo.findById(id).orElse(null);

        if (result == null) {
            throw new AgendaItemBadArgException(String.format("Invalid agendaItem id %s", id));
        } else if(!isAdmin) {
            if(!currentUser.getId().equals(result.getCreatedbyid())) {
                throw new AgendaItemBadArgException(String.format("Member %s is unauthorized to do this operation", currentUser.getId()));
            }
        agendaItemRepo.deleteById(id);
        }

    }

}


