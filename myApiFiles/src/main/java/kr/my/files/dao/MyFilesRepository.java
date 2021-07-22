package kr.my.files.dao;

import kr.my.files.entity.MyFiles;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MyFilesRepository extends CrudRepository<MyFiles, Long> {
}
