package dev.klax.sports.repository;

import dev.klax.sports.datamodel.Sport;
import dev.klax.sports.datamodel.SportsClub;
import org.springframework.data.repository.CrudRepository;

import java.util.UUID;

public interface SportsClubRepository  extends CrudRepository<SportsClub, UUID> {
}
