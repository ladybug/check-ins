package com.objectcomputing.checkins.services.agenda_item;

import com.objectcomputing.checkins.services.checkins.CheckInRepository;
import com.objectcomputing.checkins.services.memberprofile.MemberProfileRepository;

import javax.inject.Inject;
import javax.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class AgendaItemServicesImpl implements AgendaItemServices {

    @Inject
    private CheckInRepository checkinRepo;
    @Inject
    private AgendaItemRepository agendaItemRepo;
    @Inject
    private MemberProfileRepository memberRepo;

    public AgendaItem save(AgendaItem agendaItem) {
        AgendaItem agendaItemRet = null;
        if (agendaItem != null) {
            final UUID checkinId = agendaItem.getCheckinid();
            final UUID createById = agendaItem.getCreatedbyid();
            if (checkinId == null || createById == null) {
                throw new AgendaItemBadArgException(String.format("Invalid agendaItem %s", agendaItem));
            } else if (agendaItem.getId() != null) {
                throw new AgendaItemBadArgException(String.format("Found unexpected id %s for agenda item", agendaItem.getId()));
            } else if (!checkinRepo.findById(checkinId).isPresent()) {
                throw new AgendaItemBadArgException(String.format("CheckIn %s doesn't exist", checkinId));
            } else if (!memberRepo.findById(createById).isPresent()) {
                throw new AgendaItemBadArgException(String.format("Member %s doesn't exist", createById));
            }

            agendaItemRet = agendaItemRepo.save(agendaItem);
        }
        return agendaItemRet;
    }

    public AgendaItem read(@NotNull UUID id) {
        return agendaItemRepo.findById(id).orElse(null);

    }

    public Set<AgendaItem> readAll() {
        Set<AgendaItem> agendaItems = new HashSet<>();
        agendaItemRepo.findAll().forEach(agendaItems::add);
        return agendaItems;
    }

    public AgendaItem update(AgendaItem agendaItem) {
        AgendaItem agendaItemRet = null;
        if (agendaItem != null) {
            final UUID id = agendaItem.getId();
            final UUID checkinId = agendaItem.getCheckinid();
            final UUID createById = agendaItem.getCreatedbyid();
            if (checkinId == null || createById == null) {
                throw new AgendaItemBadArgException(String.format("Invalid agendaItem %s", agendaItem));
            } else if (id == null || !agendaItemRepo.findById(id).isPresent()) {
                throw new AgendaItemBadArgException(String.format("Unable to locate agendaItem to update with id %s", id));
            } else if (!checkinRepo.findById(checkinId).isPresent()) {
                throw new AgendaItemBadArgException(String.format("CheckIn %s doesn't exist", checkinId));
            } else if (!memberRepo.findById(createById).isPresent()) {
                throw new AgendaItemBadArgException(String.format("Member %s doesn't exist", createById));
            }

            agendaItemRet = agendaItemRepo.update(agendaItem);
        }
        return agendaItemRet;
    }

    public Set<AgendaItem> findByFields(UUID checkinid, UUID createdbyid) {
        Set<AgendaItem> agendaItems = new HashSet<>();
        agendaItemRepo.findAll().forEach(agendaItems::add);

        if (checkinid != null) {
            agendaItems.retainAll(agendaItemRepo.findByCheckinid(checkinid));
        }
        if (createdbyid != null) {
            agendaItems.retainAll(agendaItemRepo.findByCreatedbyid(createdbyid));
        }

        return agendaItems;
    }

    public void delete(@NotNull UUID id) {
        agendaItemRepo.deleteById(id);
    }
}