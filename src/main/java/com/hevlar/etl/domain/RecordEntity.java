package com.hevlar.etl.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table("records")
public class RecordEntity {

    @Id
    private Long id;

    @Column("external_id")
    private Long externalId;

    @Column("title")
    private String title;

    @Column("content")
    private String content;

    @Column("source")
    private String source;
}
