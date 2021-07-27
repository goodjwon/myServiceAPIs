package kr.my.files.api;

import kr.my.files.dto.FileMetadataResponse;
import kr.my.files.entity.MyFiles;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class FileAssembler extends RepresentationModelAssemblerSupport<MyFiles, FileMetadataResponse> {
    public FileAssembler() {
        super(FileController.class, FileMetadataResponse.class);
    }

    @Override
    public FileMetadataResponse toModel(MyFiles entity) {

        FileMetadataResponse fileMetadata
                = FileMetadataResponse.builder().build();
        FileMetadataResponse.builder().myFiles(entity).build();

        WebMvcLinkBuilder selfLinkBuilder = linkTo(FileController.class);
        fileMetadata.add(selfLinkBuilder.withRel("query-file"));
        fileMetadata.add(selfLinkBuilder.slash(entity.getFileDownloadPath()).withSelfRel());

        return fileMetadata;
    }
}