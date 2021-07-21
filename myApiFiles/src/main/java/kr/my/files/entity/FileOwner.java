package kr.my.files.entity;

import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import com.fasterxml.jackson.annotation.ObjectIdGenerators;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.List;

/**
 * Created by goodjwon on 16. 1. 16..
 */
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Getter
@Entity
@Table(name = "FILE_OWNER")
public class FileOwner extends BaseTimeEntity {
    @Id
    @Column(name = "OWNER_SEQ", nullable = false, insertable = true, updatable = true)
    private Long ownerSeq;

    @Column(name = "OWNER_DOMAIN_CODE", nullable = false, insertable = true, updatable = true, length = 255)
    private String userDomain;

    @Column(name = "OWNER_AUTHENTICATION_CODE", nullable = false, insertable = true, updatable = true, length = 255)
    private String userAuthenticationCode;

    /**
     * 사용자를 조회 하면 파일들(목록)이 나오게 한다.
     */
    @OneToMany(mappedBy = "fileOwnerByUserCode")
    private List<MyFiles> myFilesList;
}
