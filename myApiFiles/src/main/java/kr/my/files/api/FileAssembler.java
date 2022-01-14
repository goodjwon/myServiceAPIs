package kr.my.files.api;

import kr.my.files.dto.FileMetadataResponse;
import kr.my.files.entity.MyFiles;
import org.springframework.hateoas.Link;
import org.springframework.hateoas.server.mvc.RepresentationModelAssemblerSupport;
import org.springframework.hateoas.server.mvc.WebMvcLinkBuilder;
import org.springframework.stereotype.Component;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;

@Component
public class FileAssembler extends RepresentationModelAssemblerSupport<FileMetadataResponse, FileMetadataResponse> {
    public FileAssembler() {
        super(FileController.class, FileMetadataResponse.class);
    }

    @Override
    public FileMetadataResponse toModel(FileMetadataResponse fileMetadata) {

        WebMvcLinkBuilder selfLinkBuilder = linkTo(FileController.class);

        fileMetadata.add(selfLinkBuilder.withRel("query-file"));
        fileMetadata.add(selfLinkBuilder.slash(fileMetadata.getFileDownloadUri()).withSelfRel());
        fileMetadata.add(selfLinkBuilder.slash(fileMetadata.getFileName()).withSelfRel());
//        fileMetadata.add(selfLinkBuilder.slash(fileMetadata.getFileDownloadUri()).withRel("download-filer"));
//        fileMetadata.add(Link.of("/docs/index.html").withRel("profile"));

        return fileMetadata;
    }
}