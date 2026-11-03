package org.cpts422.parking.setup;

import java.time.LocalDate;

import org.cpts422.parking.entities.Citation;
import org.cpts422.parking.entities.Permit;
import org.cpts422.parking.entities.User;
import org.cpts422.parking.entities.UserType;
import org.cpts422.parking.entities.Zone;
import org.cpts422.parking.integrations.DateProvider;
import org.cpts422.parking.repositories.CitationRepository;
import org.cpts422.parking.repositories.PermitRepository;
import org.cpts422.parking.repositories.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Loads the four sample users, with their sample permit and citation, at startup. */
@Component
public class InitialSetup implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(InitialSetup.class);

    private final UserRepository userRepository;
    private final PermitRepository permitRepository;
    private final CitationRepository citationRepository;
    private final DateProvider dateProvider;

    public InitialSetup(UserRepository userRepository,
                        PermitRepository permitRepository,
                        CitationRepository citationRepository,
                        DateProvider dateProvider) {
        this.userRepository = userRepository;
        this.permitRepository = permitRepository;
        this.citationRepository = citationRepository;
        this.dateProvider = dateProvider;
    }

    @Override
    public void run(String... args) {
        LocalDate today = dateProvider.today();

        User alex = userRepository.save(new User("Alex Rivera", UserType.STUDENT, "CPK1001"));
        log.info("Saved user id={} name={} type={} plate={} permits=0 citations=0",
                alex.getId(), alex.getName(), alex.getType(), alex.getLicensePlate());

        User jordan = userRepository.save(new User("Jordan Lee", UserType.STUDENT, "CPK1002"));
        citationRepository.save(new Citation(jordan, today.minusDays(10), 2500));
        log.info("Saved user id={} name={} type={} plate={} permits=0 citations=1",
                jordan.getId(), jordan.getName(), jordan.getType(), jordan.getLicensePlate());

        User priya = userRepository.save(new User("Priya Shah", UserType.EMPLOYEE, "CPK2001"));
        permitRepository.save(new Permit(priya, Zone.RED, 24000, today.minusDays(30)));
        log.info("Saved user id={} name={} type={} plate={} permits=1 citations=0",
                priya.getId(), priya.getName(), priya.getType(), priya.getLicensePlate());

        User sam = userRepository.save(new User("Sam Carter", UserType.EMPLOYEE, "CPK2002"));
        log.info("Saved user id={} name={} type={} plate={} permits=0 citations=0",
                sam.getId(), sam.getName(), sam.getType(), sam.getLicensePlate());
    }
}
