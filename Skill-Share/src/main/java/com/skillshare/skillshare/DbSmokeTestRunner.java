package com.skillshare.skillshare;

import com.skillshare.skillshare.entity.DbProbe;
import com.skillshare.skillshare.repository.DbProbeRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DbSmokeTestRunner implements CommandLineRunner {

    private final DbProbeRepository repo;

    public DbSmokeTestRunner(DbProbeRepository repo) {
        this.repo = repo;
    }

    @Override
    public void run(String... args) {
        repo.save(new DbProbe("DB connection + JPA OK"));
        System.out.println("✅ DB Smoke Test: Inserted db_probe row successfully.");
    }
}