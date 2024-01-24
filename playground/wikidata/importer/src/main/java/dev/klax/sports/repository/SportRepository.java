package dev.klax.sports.repository;

import dev.klax.sports.datamodel.Sport;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SportRepository extends CrudRepository<Sport, UUID> {
}
