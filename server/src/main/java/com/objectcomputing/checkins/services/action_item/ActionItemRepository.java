package com.objectcomputing.checkins.services.action_item;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.data.jdbc.annotation.JdbcRepository;
import io.micronaut.data.model.query.builder.sql.Dialect;
import io.micronaut.data.repository.CrudRepository;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.UUID;

@JdbcRepository(dialect = Dialect.POSTGRES)
public interface ActionItemRepository extends CrudRepository<ActionItem, UUID> {

    List<ActionItem> findByCheckinid(UUID checkinid);

    List<ActionItem> findByCreatedbyid(UUID uuid);

    @Override
    <S extends ActionItem> List<S> saveAll(@Valid @NotNull Iterable<S> entities);

    @Override
    <S extends ActionItem> S save(@Valid @NotNull @NonNull S entity);

}