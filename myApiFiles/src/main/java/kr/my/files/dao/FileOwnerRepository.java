package kr.my.files.dao;

import kr.my.files.entity.FileOwner;
import org.springframework.data.repository.CrudRepository;

public interface FileOwnerRepository extends CrudRepository <FileOwner, Long> {
}
