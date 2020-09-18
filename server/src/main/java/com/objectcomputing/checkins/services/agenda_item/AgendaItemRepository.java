package com.objectcomputing.checkins.services.agenda_item;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

// @JdbcRepository(dialect = Dialect.POSTGRES)
// public interface AgendaItemRepository extends CrudRepository<AgendaItem, UUID> {

//     List<AgendaItem> findByCheckinid(UUID checkinid);

//     List<AgendaItem> findByCreatedbyid(UUID id);

//     @Override
//     <S extends AgendaItem> List<S> saveAll(@Valid @NotNull Iterable<S> entities);

//     @Override
//     <S extends AgendaItem> S save(@Valid @NotNull @NonNull S entity);

// }


import io.micronaut.data.annotation.Query;


import javax.annotation.Nullable;

import java.util.Set;


@JdbcRepository(dialect = Dialect.POSTGRES)
public interface AgendaItemRepository extends CrudRepository<AgendaItem, UUID> {

    @Query("SELECT * " +
            "FROM action_items item " +
            "WHERE (:checkinId IS NULL OR item.checkinid = :checkinId) " +
            "AND (:createdById IS NULL OR item.createdbyid = :createdById)")
    Set<AgendaItem> search(@Nullable String checkinId, @Nullable String createdById);

    @Override
    <S extends AgendaItem> List<S> saveAll(@Valid @NotNull Iterable<S> entities);

    @Override
    <S extends AgendaItem> S save(@Valid @NotNull @NonNull S entity);

}
