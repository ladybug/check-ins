package com.objectcomputing.checkins.services.agenda_item;

import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Error;
import io.micronaut.http.annotation.*;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.swagger.v3.oas.annotations.tags.Tag;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.objectcomputing.checkins.services.memberprofile.MemberProfile;	
import com.objectcomputing.checkins.services.memberprofile.currentuser.CurrentUserServicesImpl;	
import com.objectcomputing.checkins.services.role.RoleType;
import io.micronaut.http.HttpStatus;	
import io.micronaut.security.utils.SecurityService;	

@Controller("/services/agenda-item")
@Secured(SecurityRule.IS_AUTHENTICATED)
@Produces(MediaType.APPLICATION_JSON)
@Tag(name = "agenda-item")
public class AgendaItemController {

    @Inject
    AgendaItemServices agendaItemServices;

    @Inject	
    CurrentUserServicesImpl currentUserServices;	
    @Inject	
    SecurityService securityService;

    @Error(exception = AgendaItemBadArgException.class)
    public HttpResponse<?> handleBadArgs(HttpRequest<?> request, AgendaItemBadArgException e) {
        JsonError error = new JsonError(e.getMessage())
                .link(Link.SELF, Link.of(request.getUri()));

        return HttpResponse.<JsonError>badRequest()
                .body(error);
    }

    // /**
    //  * Create and save a new agenda item.
    //  *
    //  * @param agendaItem, {@link AgendaItemCreateDTO}
    //  * @return {@link HttpResponse <AgendaItem>}
    //  */
    // @Post()
    // public HttpResponse<AgendaItem> createAgendaItem(@Body @Valid AgendaItemCreateDTO agendaItem,
    //                                                  HttpRequest<AgendaItemCreateDTO> request) {
    //     AgendaItem newAgendaItem = agendaItemServices.save(new AgendaItem(agendaItem.getCheckinid(),
    //             agendaItem.getCreatedbyid(), agendaItem.getDescription()));
    //     return HttpResponse
    //             .created(newAgendaItem)
    //             .headers(headers -> headers.location(
    //                     URI.create(String.format("%s/%s", request.getPath(), newAgendaItem.getId()))));
    // }

    /**	
     * Save check-in details.	
     * @param agendaItem	
     * @return	
     */	
    @Post()	
    public HttpResponse<AgendaItem> createCheckIn(@Body @Valid AgendaItemCreateDTO agendaItem, HttpRequest<AgendaItemCreateDTO> request) {	
        MemberProfile currentUser = currentUserServices.currentUserDetails();	
        Boolean isAdmin = securityService.hasRole(RoleType.Constants.ADMIN_ROLE);	
        if(currentUser.getUuid().equals(agendaItem.getCreatedbyid()) || currentUser.getUuid().equals(agendaItem.getCheckinid()) || isAdmin) {	
            AgendaItem newMemberCheckIn = agendaItemServices.save(new AgendaItem(agendaItem.getCheckinid(),agendaItem.getCreatedbyid(), agendaItem.getDescription()));	
            return HttpResponse.created(newMemberCheckIn)	
                    .headers(headers -> headers.location(URI.create(String.format("%s/%s", request.getPath(), newMemberCheckIn.getId()))));	
        }	
        return HttpResponse.status(HttpStatus.FORBIDDEN);	
    }


    // /**
    //  * Update agenda item.
    //  *
    //  * @param agendaItem, {@link AgendaItem}
    //  * @return {@link HttpResponse< AgendaItem >}
    //  */
    // @Put()
    // public HttpResponse<?> updateAgendaItem(@Body @Valid AgendaItem agendaItem, HttpRequest<AgendaItem> request) {
    //     AgendaItem updatedAgendaItem = agendaItemServices.update(agendaItem);
    //     return HttpResponse
    //             .ok()
    //             .headers(headers -> headers.location(
    //                     URI.create(String.format("%s/%s", request.getPath(), updatedAgendaItem.getId()))))
    //             .body(updatedAgendaItem);

    // }

	    /**	
     * Update check in details	
     * @param agendaItem	
     * @return	
     */	
    @Put("/")	
    public HttpResponse<?> update(@Body @Valid AgendaItem agendaItem, HttpRequest<AgendaItemCreateDTO> request) {	
        MemberProfile currentUser = currentUserServices.currentUserDetails();	
        Boolean isAdmin = securityService.hasRole(RoleType.Constants.ADMIN_ROLE);	
        if(currentUser.getUuid().equals(agendaItem.getCreatedbyid()) || currentUser.getUuid().equals(agendaItem.getCheckinid()) || isAdmin) {	
            AgendaItem updatedMemberCheckIn = agendaItemServices.update(agendaItem);	
            return HttpResponse	
                    .ok()	
                    .headers(headers -> headers.location(URI.create(String.format("%s/%s", request.getPath(), updatedMemberCheckIn.getId()))))	
                    .body(updatedMemberCheckIn);	
        }	
        return HttpResponse.status(HttpStatus.FORBIDDEN);	
    }



    /**
     * Delete agenda item
     *
     * @param id, id of {@link AgendaItem} to delete
     */
    @Delete("/{id}")
    public HttpResponse<?> deleteAgendaItem(UUID id) {
        agendaItemServices.delete(id);
        return HttpResponse
                .ok();
    }

    // /**
    //  * Get agenda item based off id
    //  *
    //  * @param id {@link UUID} of the agenda item entry
    //  * @return {@link AgendaItem}
    //  */
    // @Get("/{id}")
    // public AgendaItem readAgendaItem(UUID id) {
    //     return agendaItemServices.read(id);
    // }


	/**	
     * 	
     * @param id	
     * @return	
     */	
    @Get("/{id}")	
    public AgendaItem readAgendaItem(@NotNull UUID id){	
        MemberProfile currentUser = currentUserServices.currentUserDetails();	
        Boolean isAdmin = securityService.hasRole(RoleType.Constants.ADMIN_ROLE);	
        AgendaItem agendaItem = agendaItemServices.read(id);	
        if(currentUser.getUuid().equals(agendaItem.getCreatedbyid()) || currentUser.getUuid().equals(agendaItem.getCheckinid()) || isAdmin) {	
            return agendaItem;	
        }	
        return null;	
    }

    /**
     * Find agenda items that match all filled in parameters, return all results when given no params
     *
     * @param checkinid   {@link UUID} of checkin
     * @param createdbyid {@link UUID} of member
     * @return {@link List < CheckIn > list of checkins}
     */
    @Get("/{?checkinid,createdbyid}")
    public Set<AgendaItem> findAgendaItems(@Nullable UUID checkinid,
                                           @Nullable UUID createdbyid) {
        return agendaItemServices.findByFields(checkinid, createdbyid);
    }


	// // /**	
    // //  * Find AgendaItem details by checkinid or createdbyid. 	
    // //  * @param checkinid	
    // //  * @param createdbyid	
    // //  * @return	
    // //  */	
    // @Get("/{?checkinid, createdbyid}")	
    // public Set<AgendaItem> findByValue(@Nullable UUID checkinid, @Nullable UUID  createdbyid) {	
    //     MemberProfile currentUser = currentUserServices.currentUserDetails();	
    //     Boolean isAdmin = securityService.hasRole(RoleType.Constants.ADMIN_ROLE);	
    //     // public Set<AgendaItem> findAgendaItems(@Nullable UUID checkinid, @Nullable UUID createdbyid);
    //     Set<AgendaItem> agendaItemResult = agendaItemServices.findByFields(checkinid, createdbyid);	
    //     if(isAdmin ||	
    //             agendaItemResult.stream().allMatch(agendaItem -> agendaItem.getCreatedbyid().equals(currentUser.getUuid())) ||	
    //             agendaItemResult.stream().anyMatch(agendaItem -> agendaItem.getCheckinid().equals(currentUser.getUuid()))) {	
    //         return agendaItemResult;	
    //     }	
    //     return null;	
    // }


    /**
     * Load agenda items
     *
     * @param agendaItems, {@link List< AgendaItemCreateDTO > to load {@link AgendaItem agenda items}}
     * @return {@link HttpResponse<List< AgendaItem >}
     */
    @Post("/items")
    public HttpResponse<?> loadAgendaItems(@Body @Valid @NotNull List<AgendaItemCreateDTO> agendaItems,
                                           HttpRequest<List<AgendaItem>> request) {
        List<String> errors = new ArrayList<>();
        List<AgendaItem> agendaItemsCreated = new ArrayList<>();
        for (AgendaItemCreateDTO agendaItemDTO : agendaItems) {
            AgendaItem agendaItem = new AgendaItem(agendaItemDTO.getCheckinid(),
                    agendaItemDTO.getCreatedbyid(), agendaItemDTO.getDescription());
            try {
                agendaItemServices.save(agendaItem);
                agendaItemsCreated.add(agendaItem);
            } catch (AgendaItemBadArgException e) {
                errors.add(String.format("Member %s's agenda item was not added to CheckIn %s because: %s", agendaItem.getCreatedbyid(),
                        agendaItem.getCheckinid(), e.getMessage()));
            }
        }
        if (errors.isEmpty()) {
            return HttpResponse.created(agendaItemsCreated)
                    .headers(headers -> headers.location(request.getUri()));
        } else {
            return HttpResponse.badRequest(errors)
                    .headers(headers -> headers.location(request.getUri()));
        }
    }
}

