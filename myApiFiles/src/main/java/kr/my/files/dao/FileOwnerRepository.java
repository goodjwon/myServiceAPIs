package kr.my.files.dao;

import kr.my.files.entity.FileOwner;
import org.springframework.data.repository.CrudRepository;

import java.util.Optional;

public interface FileOwnerRepository extends CrudRepository <FileOwner, Long> {
    Optional<FileOwner> findByOwnerDomainCheckSumAndOwnerAuthenticationCheckSum(String domain, String userCode);
}
