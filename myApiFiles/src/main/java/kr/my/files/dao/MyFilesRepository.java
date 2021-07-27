package kr.my.files.dao;

import kr.my.files.entity.FileOwner;
import kr.my.files.entity.MyFiles;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MyFilesRepository extends CrudRepository<MyFiles, Long> {
    Optional<MyFiles> findByFilePhyNameAndFileHashCode(String filePhyName, String fileHashCode);
}
