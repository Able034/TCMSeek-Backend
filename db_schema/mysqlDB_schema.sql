create table QRTZ_CALENDARS
(
    sched_name    varchar(120) not null comment '调度名称',
    calendar_name varchar(200) not null comment '日历名称',
    calendar      blob         not null comment '存放持久化calendar对象',
    primary key (sched_name, calendar_name)
)
    comment '日历信息表';

create table QRTZ_FIRED_TRIGGERS
(
    sched_name        varchar(120) not null comment '调度名称',
    entry_id          varchar(95)  not null comment '调度器实例id',
    trigger_name      varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group     varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    instance_name     varchar(200) not null comment '调度器实例名',
    fired_time        bigint       not null comment '触发的时间',
    sched_time        bigint       not null comment '定时器制定的时间',
    priority          int          not null comment '优先级',
    state             varchar(16)  not null comment '状态',
    job_name          varchar(200) null comment '任务名称',
    job_group         varchar(200) null comment '任务组名',
    is_nonconcurrent  varchar(1)   null comment '是否并发',
    requests_recovery varchar(1)   null comment '是否接受恢复执行',
    primary key (sched_name, entry_id)
)
    comment '已触发的触发器表';

create table QRTZ_JOB_DETAILS
(
    sched_name        varchar(120) not null comment '调度名称',
    job_name          varchar(200) not null comment '任务名称',
    job_group         varchar(200) not null comment '任务组名',
    description       varchar(250) null comment '相关介绍',
    job_class_name    varchar(250) not null comment '执行任务类名称',
    is_durable        varchar(1)   not null comment '是否持久化',
    is_nonconcurrent  varchar(1)   not null comment '是否并发',
    is_update_data    varchar(1)   not null comment '是否更新数据',
    requests_recovery varchar(1)   not null comment '是否接受恢复执行',
    job_data          blob         null comment '存放持久化job对象',
    primary key (sched_name, job_name, job_group)
)
    comment '任务详细信息表';

create table QRTZ_LOCKS
(
    sched_name varchar(120) not null comment '调度名称',
    lock_name  varchar(40)  not null comment '悲观锁名称',
    primary key (sched_name, lock_name)
)
    comment '存储的悲观锁信息表';

create table QRTZ_PAUSED_TRIGGER_GRPS
(
    sched_name    varchar(120) not null comment '调度名称',
    trigger_group varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    primary key (sched_name, trigger_group)
)
    comment '暂停的触发器表';

create table QRTZ_SCHEDULER_STATE
(
    sched_name        varchar(120) not null comment '调度名称',
    instance_name     varchar(200) not null comment '实例名称',
    last_checkin_time bigint       not null comment '上次检查时间',
    checkin_interval  bigint       not null comment '检查间隔时间',
    primary key (sched_name, instance_name)
)
    comment '调度器状态表';

create table compound_admet
(
    id                  int auto_increment
        primary key,
    inchikey            varchar(100)                        not null comment '化合物InChIKey',
    ames                double                              null comment 'Ames致突变性',
    bbbp                double                              null comment '血脑屏障通透性',
    bioavailability     int                                 null comment '生物利用度',
    caco2               double                              null comment 'Caco-2通透性',
    carcinogens         int                                 null comment '致癌性',
    clearance_microsome double                              null comment '微粒体清除率',
    clintox             int                                 null comment '临床毒性',
    cyp1a2_inhibition   double                              null comment 'CYP1A2抑制',
    cyp2c19_inhibition  double                              null comment 'CYP2C19抑制',
    cyp2c9_inhibition   int                                 null comment 'CYP2C9抑制',
    cyp2c9_substrate    double                              null comment 'CYP2C9底物',
    cyp2d6_inhibition   int                                 null comment 'CYP2D6抑制',
    cyp2d6_substrate    double                              null comment 'CYP2D6底物',
    cyp3a4_inhibition   double                              null comment 'CYP3A4抑制',
    cyp3a4_substrate    double                              null comment 'CYP3A4底物',
    dili                double                              null comment '药物性肝损伤',
    freesolv            double                              null comment '自由溶解度',
    herg_blockers       double                              null comment 'hERG阻断',
    herg_karim          double                              null comment 'hERG Karim',
    hia                 int                                 null comment '人体肠道吸收',
    ld50                double                              null comment '半致死量',
    lipophilicity       double                              null comment '亲脂性',
    pampa               double                              null comment 'PAMPA渗透性',
    pgp                 double                              null comment 'P-糖蛋白底物',
    ppbr                double                              null comment '血浆蛋白结合率',
    skin                double                              null comment '皮肤渗透性',
    solubility          double                              null comment '溶解度',
    created_at          timestamp default CURRENT_TIMESTAMP null,
    updated_at          timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint inchikey
        unique (inchikey)
)
    comment '化合物ADMET预测结果表' collate = utf8mb4_unicode_ci;

create index idx_bioavailability
    on compound_admet (bioavailability);

create index idx_ld50
    on compound_admet (ld50);

create table compound_target_rel
(
    id         int auto_increment
        primary key,
    inchikey   varchar(100)                        not null comment '化合物InChIKey',
    tcm_tar_id varchar(50)                         not null comment '靶标ID',
    source     varchar(100)                        null comment '数据来源',
    created_at timestamp default CURRENT_TIMESTAMP null,
    constraint uk_comp_tar
        unique (inchikey, tcm_tar_id, source)
)
    comment '化合物-靶标关联表' collate = utf8mb4_unicode_ci;

create index idx_compound
    on compound_target_rel (inchikey);

create index idx_source
    on compound_target_rel (source);

create index idx_target
    on compound_target_rel (tcm_tar_id);

create table compound_transcriptomics
(
    id             int auto_increment
        primary key,
    inchikey       varchar(100)                        not null comment '化合物InChIKey',
    gene_entrez_id int                                 null comment '基因Entrez ID',
    log2_fc_avg    double                              null comment 'Log2倍数变化平均值',
    p_value        double                              null comment 'P值',
    direction      varchar(20)                         null comment '方向 (up/down)',
    source         varchar(100)                        null comment '数据来源',
    created_at     timestamp default CURRENT_TIMESTAMP null,
    gen            varchar(100)                        null
)
    comment '化合物转录组学数据表' collate = utf8mb4_unicode_ci;

create index idx_compound
    on compound_transcriptomics (inchikey);

create index idx_direction
    on compound_transcriptomics (direction);

create index idx_gene
    on compound_transcriptomics (gene_entrez_id);

create table core_tcm_herbs
(
    id                   int auto_increment comment '自增主键'
        primary key,
    tcm_herb_id          varchar(50)                         not null comment '中药唯一标识ID (HERB_1)',
    herb_name_zh         varchar(50)                         not null comment '中药名称（中文）',
    pinyin_name          varchar(100)                        null comment '拼音名称',
    latin_name           varchar(500)                        null comment '拉丁名称',
    english_name         varchar(255)                        null comment '英文名称',
    type                 varchar(100)                        null comment '药材类型',
    efficacy_zh          varchar(100)                        null comment '功效（中文）',
    function_en          text                                null comment '功能描述（英文）',
    efficacy_category    varchar(100)                        null comment '功效分类',
    toxicity_zh          varchar(50)                         null comment '毒性（中文）',
    toxic_en             varchar(50)                         null comment '毒性（英文）',
    toxic_description_zh varchar(500)                        null comment '毒性描述（中文）',
    toxic_effect_en      varchar(500)                        null comment '毒性效应（英文）',
    pharmacopoeia_record varchar(100)                        null comment '药典收录情况',
    classification       varchar(100)                        null comment '生物学分类（科名）',
    use_part             varchar(255)                        null comment '使用部位',
    nature_taste_zh      varchar(50)                         null comment '性味（中文）',
    property_en          varchar(255)                        null comment '性味（英文）',
    meridian_zh          varchar(50)                         null comment '归经（中文）',
    meridian_tropism_en  varchar(255)                        null comment '归经（英文）',
    indications_zh       text                                null comment '主治（中文）',
    indication_en        text                                null comment '主治（英文）',
    created_at           timestamp default CURRENT_TIMESTAMP null,
    updated_at           timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    classification_zh    varchar(255)                        null,
    constraint tcm_herb_id
        unique (tcm_herb_id)
)
    comment '核心中药信息表' collate = utf8mb4_unicode_ci;

create fulltext index ft_efficacy
    on core_tcm_herbs (efficacy_zh, indications_zh);

create index idx_herb_name
    on core_tcm_herbs (herb_name_zh);

create index idx_pinyin
    on core_tcm_herbs (pinyin_name);

create index idx_type
    on core_tcm_herbs (type);

create table database_statistics
(
    id           int auto_increment
        primary key,
    table_name   varchar(100)                        not null comment '表名',
    category     varchar(50)                         not null comment '类别(entity/relation/transcriptomics)',
    record_count int       default 0                 null comment '记录数',
    description  text                                null comment '描述',
    last_updated timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint uk_table
        unique (table_name)
)
    comment '数据库统计信息表' collate = utf8mb4_unicode_ci;

create table disease_gene_rel
(
    id         int auto_increment
        primary key,
    tcm_tar_id varchar(50)                         not null comment '靶标ID',
    disease_id varchar(50)                         not null comment '疾病ID',
    relation   varchar(255)                        null comment '关系类型',
    source     text                                null comment '数据来源（PMID等）',
    created_at timestamp default CURRENT_TIMESTAMP null
)
    comment '疾病-基因关联表' collate = utf8mb4_unicode_ci;

create index idx_disease
    on disease_gene_rel (disease_id);

create index idx_target
    on disease_gene_rel (tcm_tar_id);

create table diseases
(
    id           int auto_increment
        primary key,
    disease_id   varchar(50)                         not null comment '疾病ID (DOID:11832)',
    disease_name varchar(255)                        null,
    source       varchar(50)                         null comment '数据来源',
    created_at   timestamp default CURRENT_TIMESTAMP null,
    updated_at   timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint disease_id
        unique (disease_id)
)
    comment '疾病信息表' collate = utf8mb4_unicode_ci;

create index idx_disease_name
    on diseases (disease_name);

create table gen_table
(
    table_id          bigint auto_increment comment '编号'
        primary key,
    table_name        varchar(200) default ''     null comment '表名称',
    table_comment     varchar(500) default ''     null comment '表描述',
    sub_table_name    varchar(64)                 null comment '关联子表的表名',
    sub_table_fk_name varchar(64)                 null comment '子表关联的外键名',
    class_name        varchar(100) default ''     null comment '实体类名称',
    tpl_category      varchar(200) default 'crud' null comment '使用的模板（crud单表操作 tree树表操作）',
    tpl_web_type      varchar(30)  default ''     null comment '前端模板类型（element-ui模版 element-plus模版）',
    package_name      varchar(100)                null comment '生成包路径',
    module_name       varchar(30)                 null comment '生成模块名',
    business_name     varchar(30)                 null comment '生成业务名',
    function_name     varchar(50)                 null comment '生成功能名',
    function_author   varchar(50)                 null comment '生成功能作者',
    gen_type          char         default '0'    null comment '生成代码方式（0zip压缩包 1自定义路径）',
    gen_path          varchar(200) default '/'    null comment '生成路径（不填默认项目路径）',
    options           varchar(1000)               null comment '其它生成选项',
    create_by         varchar(64)  default ''     null comment '创建者',
    create_time       datetime                    null comment '创建时间',
    update_by         varchar(64)  default ''     null comment '更新者',
    update_time       datetime                    null comment '更新时间',
    remark            varchar(500)                null comment '备注'
)
    comment '代码生成业务表';

create table gen_table_column
(
    column_id      bigint auto_increment comment '编号'
        primary key,
    table_id       bigint                    null comment '归属表编号',
    column_name    varchar(200)              null comment '列名称',
    column_comment varchar(500)              null comment '列描述',
    column_type    varchar(100)              null comment '列类型',
    java_type      varchar(500)              null comment 'JAVA类型',
    java_field     varchar(200)              null comment 'JAVA字段名',
    is_pk          char                      null comment '是否主键（1是）',
    is_increment   char                      null comment '是否自增（1是）',
    is_required    char                      null comment '是否必填（1是）',
    is_insert      char                      null comment '是否为插入字段（1是）',
    is_edit        char                      null comment '是否编辑字段（1是）',
    is_list        char                      null comment '是否列表字段（1是）',
    is_query       char                      null comment '是否查询字段（1是）',
    query_type     varchar(200) default 'EQ' null comment '查询方式（等于、不等于、大于、小于、范围）',
    html_type      varchar(200)              null comment '显示类型（文本框、文本域、下拉框、复选框、单选框、日期控件）',
    dict_type      varchar(200) default ''   null comment '字典类型',
    sort           int                       null comment '排序',
    create_by      varchar(64)  default ''   null comment '创建者',
    create_time    datetime                  null comment '创建时间',
    update_by      varchar(64)  default ''   null comment '更新者',
    update_time    datetime                  null comment '更新时间'
)
    comment '代码生成业务表字段';

create table herb_compound_rel
(
    id          int auto_increment
        primary key,
    tcm_herb_id varchar(50)                         not null comment '中药ID',
    inchikey    varchar(100)                        not null comment '化合物InChIKey',
    annotation  varchar(255)                        null comment '注释 (Blood ingredients等)',
    source      varchar(100)                        null comment '数据来源',
    created_at  timestamp default CURRENT_TIMESTAMP null,
    constraint uk_herb_comp
        unique (tcm_herb_id, inchikey, source)
)
    comment '中药-化合物关联表' collate = utf8mb4_unicode_ci;

create index idx_compound
    on herb_compound_rel (inchikey);

create index idx_herb
    on herb_compound_rel (tcm_herb_id);

create index idx_source
    on herb_compound_rel (source);

create table herb_disease_rel
(
    id          int auto_increment
        primary key,
    tcm_herb_id varchar(50)                         not null comment '核心中药ID',
    disease_id  varchar(100)                        not null comment '疾病ID',
    created_at  timestamp default CURRENT_TIMESTAMP null,
    constraint uk_herb_dis
        unique (tcm_herb_id, disease_id)
)
    comment '中药-疾病关联表' collate = utf8mb4_unicode_ci;

create index idx_disease
    on herb_disease_rel (disease_id);

create index idx_herb
    on herb_disease_rel (tcm_herb_id);

create table herb_symptom_rel
(
    id             int auto_increment
        primary key,
    tcm_herb_id    varchar(50)                         not null comment '中药ID',
    tcm_symptom_id varchar(50)                         not null comment '中医症状ID',
    created_at     timestamp default CURRENT_TIMESTAMP null,
    constraint uk_herb_sym
        unique (tcm_herb_id, tcm_symptom_id)
)
    comment '中药-中医症状关联表' collate = utf8mb4_unicode_ci;

create index idx_herb
    on herb_symptom_rel (tcm_herb_id);

create index idx_symptom
    on herb_symptom_rel (tcm_symptom_id);

create table herb_syndrome_rel
(
    id              int auto_increment
        primary key,
    syndrome_name   varchar(255)                        null comment '证候名称',
    tcm_herb_id     varchar(50)                         not null comment '中药ID',
    tcm_syndrome_id varchar(50)                         not null comment '证候ID',
    source          varchar(100)                        null comment '数据来源',
    created_at      timestamp default CURRENT_TIMESTAMP null,
    constraint uk_herb_syn
        unique (tcm_herb_id, tcm_syndrome_id)
)
    comment '中药-证候关联表' collate = utf8mb4_unicode_ci;

create index idx_herb
    on herb_syndrome_rel (tcm_herb_id);

create index idx_syndrome
    on herb_syndrome_rel (tcm_syndrome_id);

create table herb_transcriptomics
(
    id          int auto_increment
        primary key,
    tcm_herb_id varchar(50)                         not null comment '中药ID',
    gene_name   varchar(100)                        not null comment '基因名称',
    log2_fc_avg double                              null comment 'Log2倍数变化平均值',
    p_value     double                              null comment 'P值',
    direction   varchar(20)                         null comment '方向 (up/down)',
    source      varchar(100)                        null comment '数据来源',
    created_at  timestamp default CURRENT_TIMESTAMP null,
    constraint uk_herb_gene
        unique (tcm_herb_id, gene_name)
)
    comment '中药材转录组学数据表' collate = utf8mb4_unicode_ci;

create index idx_direction
    on herb_transcriptomics (direction);

create index idx_gene
    on herb_transcriptomics (gene_name);

create index idx_herb
    on herb_transcriptomics (tcm_herb_id);

create table medical_case_herb_rel
(
    id          int auto_increment
        primary key,
    med_case_id varchar(50)                         not null comment '医案ID',
    tcm_herb_id varchar(50)                         not null comment '核心中药ID',
    created_at  timestamp default CURRENT_TIMESTAMP null,
    constraint uk_case_herb
        unique (med_case_id, tcm_herb_id)
)
    comment '医案-核心中药关联表' collate = utf8mb4_unicode_ci;

create index idx_case
    on medical_case_herb_rel (med_case_id);

create index idx_herb
    on medical_case_herb_rel (tcm_herb_id);

create table medical_case_prescription_rel
(
    id                   int auto_increment
        primary key,
    med_case_id          varchar(50)                         not null comment '医案ID',
    tcm_prescription_id  varchar(50)                         not null comment '方剂ID',
    created_at           timestamp default CURRENT_TIMESTAMP null,
    perscription_name_zh varchar(255)                        null,
    constraint uk_case_pre
        unique (med_case_id, tcm_prescription_id)
)
    comment '医案-方剂关联表' collate = utf8mb4_unicode_ci;

create index idx_case
    on medical_case_prescription_rel (med_case_id);

create index idx_prescription
    on medical_case_prescription_rel (tcm_prescription_id);

create table medical_cases
(
    id                   int auto_increment
        primary key,
    med_case_id          varchar(50)                         not null comment '医案ID (Med_Case0001)',
    case_report          text                                null comment '医案全文报告',
    physician            varchar(100)                        null comment '医生姓名',
    tcm_disease          varchar(255)                        null comment '中医疾病',
    wm_disease           varchar(255)                        null comment '西医疾病',
    tcm_symptoms         text                                null comment '中医症状',
    wm_symptoms          text                                null comment '西医症状',
    urination_defecation text                                null comment '二便情况',
    pulse_condition      varchar(255)                        null comment '脉象',
    tongue_appearance    varchar(255)                        null comment '舌象',
    tcm_syndrome         text                                null comment '中医证候',
    tcm_treatment        text                                null comment '中医治法',
    prescription         varchar(255)                        null comment '使用方剂',
    herb_composition     text                                null comment '中药组成',
    created_at           timestamp default CURRENT_TIMESTAMP null,
    updated_at           timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint med_case_id
        unique (med_case_id)
)
    comment '医案信息表' collate = utf8mb4_unicode_ci;

create fulltext index ft_case_report
    on medical_cases (case_report);

create index idx_physician
    on medical_cases (physician);

create index idx_prescription
    on medical_cases (prescription);

create table other_tcm_herbs
(
    id                   int auto_increment
        primary key,
    tcm_herb2_id         varchar(50)                         not null comment '其他中药ID (HERB2_1)',
    herb_name_zh         varchar(100)                        null comment '药材名称',
    pinyin_name          varchar(100)                        null comment '拼音名称',
    latin_name           varchar(500)                        null comment '拉丁名称',
    english_name         varchar(255)                        null comment '英文名称',
    type                 varchar(100)                        null comment '药材类型',
    efficacy_zh          text                                null comment '功效（中文）',
    function_en          text                                null comment '功效（英文）',
    efficacy_category    text                                null comment '功效分类',
    toxicity_zh          text                                null comment '毒性（中文）',
    toxic_en             text                                null comment '毒性（英文）',
    toxic_description_zh text                                null comment '毒性描述',
    toxic_effect_en      text                                null comment '毒性效应',
    pharmacopoeia_record text                                null comment '药典收录',
    classification_zh    varchar(100)                        null comment '生物学分类（中文）',
    classification_en    varchar(100)                        null comment '生物学分类（英文）',
    use_part_en          varchar(255)                        null comment '使用部位',
    property_zh          text                                null comment '性味（中文）',
    property_en          text                                null comment '性味（英文）',
    meridian_tropism_zh  text                                null comment '归经（中文）',
    meridian_tropism_en  text                                null comment '归经（英文）',
    indication_zh        text                                null comment '主治（中文）',
    indication_en        text                                null comment '主治（英文）',
    created_at           timestamp default CURRENT_TIMESTAMP null,
    updated_at           timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint tcm_herb2_id
        unique (tcm_herb2_id)
)
    comment '其他中药信息表' collate = utf8mb4_unicode_ci;

create index idx_herb2_name
    on other_tcm_herbs (herb_name_zh);

create table pathway_target_rel
(
    id         int auto_increment
        primary key,
    pathway_id varchar(50)                         not null comment '通路ID',
    tcm_tar_id varchar(50)                         not null comment '靶标ID',
    created_at timestamp default CURRENT_TIMESTAMP null,
    constraint uk_path_tar
        unique (pathway_id, tcm_tar_id)
)
    comment '通路-靶标关联表' collate = utf8mb4_unicode_ci;

create index idx_pathway
    on pathway_target_rel (pathway_id);

create index idx_target
    on pathway_target_rel (tcm_tar_id);

create table pathways
(
    id         int auto_increment
        primary key,
    pathway_id varchar(50)                         not null comment '通路ID (hsa01100)',
    name       varchar(255)                        not null comment '通路名称',
    source     varchar(50)                         null comment '数据来源 (KEGG)',
    created_at timestamp default CURRENT_TIMESTAMP null,
    updated_at timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint pathway_id
        unique (pathway_id)
)
    comment '通路信息表' collate = utf8mb4_unicode_ci;

create index idx_pathway_name
    on pathways (name);

create table phenotype_target_rel
(
    id           int auto_increment
        primary key,
    phenotype_id varchar(50)                         not null comment '表型ID',
    tcm_tar_id   varchar(50)                         not null comment '靶标ID',
    created_at   timestamp default CURRENT_TIMESTAMP null,
    constraint uk_phe_tar
        unique (phenotype_id, tcm_tar_id)
)
    comment '表型-靶标关联表' collate = utf8mb4_unicode_ci;

create index idx_phenotype
    on phenotype_target_rel (phenotype_id);

create index idx_target
    on phenotype_target_rel (tcm_tar_id);

create table phenotypes
(
    id             int auto_increment
        primary key,
    phenotype_id   varchar(50)                         not null comment '表型ID (HP:0000006)',
    phenotype_name varchar(255)                        not null comment '表型名称',
    source         varchar(50)                         null comment '数据来源 (HPO)',
    created_at     timestamp default CURRENT_TIMESTAMP null,
    updated_at     timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint phenotype_id
        unique (phenotype_id)
)
    comment '表型信息表' collate = utf8mb4_unicode_ci;

create index idx_phenotype_name
    on phenotypes (phenotype_name);

create table prescription_core_herb_rel
(
    id                  int auto_increment
        primary key,
    tcm_prescription_id varchar(50)                         not null comment '方剂ID',
    tcm_herb_id         varchar(50)                         not null comment '核心中药ID',
    created_at          timestamp default CURRENT_TIMESTAMP null
)
    comment '方剂-核心中药关联表' collate = utf8mb4_unicode_ci;

create index idx_herb
    on prescription_core_herb_rel (tcm_herb_id);

create index idx_pchr_herb_presc
    on prescription_core_herb_rel (tcm_herb_id, tcm_prescription_id);

create index idx_prescription
    on prescription_core_herb_rel (tcm_prescription_id);

create table prescription_disease_rel
(
    id                  int auto_increment
        primary key,
    tcm_prescription_id varchar(50)                         not null comment '方剂ID',
    disease_id          varchar(50)                         not null comment '疾病ID',
    created_at          timestamp default CURRENT_TIMESTAMP null,
    constraint uk_pre_dis
        unique (tcm_prescription_id, disease_id)
)
    comment '方剂-疾病关联表' collate = utf8mb4_unicode_ci;

create index idx_disease
    on prescription_disease_rel (disease_id);

create index idx_prescription
    on prescription_disease_rel (tcm_prescription_id);

create table prescription_other_herb_rel
(
    id                  int auto_increment
        primary key,
    tcm_prescription_id varchar(50)                         not null comment '方剂ID',
    tcm_herb2_id        varchar(50)                         not null comment '其他中药ID',
    created_at          timestamp default CURRENT_TIMESTAMP null
)
    comment '方剂-其他中药关联表' collate = utf8mb4_unicode_ci;

create index idx_herb2
    on prescription_other_herb_rel (tcm_herb2_id);

create index idx_prescription
    on prescription_other_herb_rel (tcm_prescription_id);

create table prescription_symptom_rel
(
    id                  int auto_increment
        primary key,
    prescription_name   varchar(255)                        null comment '方剂名称',
    tcm_prescription_id varchar(50)                         not null comment '方剂ID',
    tcm_symptom_id      varchar(50)                         not null comment '中医症状ID',
    created_at          timestamp default CURRENT_TIMESTAMP null,
    constraint uk_pre_sym
        unique (tcm_prescription_id, tcm_symptom_id)
)
    comment '方剂-中医症状关联表' collate = utf8mb4_unicode_ci;

create index idx_prescription
    on prescription_symptom_rel (tcm_prescription_id);

create index idx_symptom
    on prescription_symptom_rel (tcm_symptom_id);

create table prescription_syndrome_rel
(
    id                  int auto_increment
        primary key,
    prescription_name   varchar(255)                        null comment '方剂名称',
    tcm_prescription_id varchar(50)                         not null comment '方剂ID',
    tcm_syndrome_id     varchar(50)                         not null comment '证候ID',
    created_at          timestamp default CURRENT_TIMESTAMP null,
    constraint uk_pre_syn
        unique (tcm_prescription_id, tcm_syndrome_id)
)
    comment '方剂-证候关联表' collate = utf8mb4_unicode_ci;

create index idx_prescription
    on prescription_syndrome_rel (tcm_prescription_id);

create index idx_syndrome
    on prescription_syndrome_rel (tcm_syndrome_id);

create table prescription_transcriptomics
(
    id                  int auto_increment
        primary key,
    tcm_prescription_id varchar(50)                         not null comment '方剂ID',
    gene_name           varchar(100)                        not null comment '基因名称',
    log2_fc_avg         double                              null comment 'Log2倍数变化平均值',
    p_value             double                              null comment 'P值',
    direction           varchar(20)                         null comment '方向 (up/down)',
    source              varchar(100)                        null comment '数据来源',
    created_at          timestamp default CURRENT_TIMESTAMP null,
    constraint uk_pre_gene
        unique (tcm_prescription_id, gene_name)
)
    comment '方剂转录组学数据表' collate = utf8mb4_unicode_ci;

create index idx_direction
    on prescription_transcriptomics (direction);

create index idx_gene
    on prescription_transcriptomics (gene_name);

create index idx_prescription
    on prescription_transcriptomics (tcm_prescription_id);

create table qrtz_calendars
(
    sched_name    varchar(120) not null comment '调度名称',
    calendar_name varchar(200) not null comment '日历名称',
    calendar      blob         not null comment '存放持久化calendar对象',
    primary key (sched_name, calendar_name)
)
    comment '日历信息表';

create table qrtz_fired_triggers
(
    sched_name        varchar(120) not null comment '调度名称',
    entry_id          varchar(95)  not null comment '调度器实例id',
    trigger_name      varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group     varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    instance_name     varchar(200) not null comment '调度器实例名',
    fired_time        bigint       not null comment '触发的时间',
    sched_time        bigint       not null comment '定时器制定的时间',
    priority          int          not null comment '优先级',
    state             varchar(16)  not null comment '状态',
    job_name          varchar(200) null comment '任务名称',
    job_group         varchar(200) null comment '任务组名',
    is_nonconcurrent  varchar(1)   null comment '是否并发',
    requests_recovery varchar(1)   null comment '是否接受恢复执行',
    primary key (sched_name, entry_id)
)
    comment '已触发的触发器表';

create table qrtz_job_details
(
    sched_name        varchar(120) not null comment '调度名称',
    job_name          varchar(200) not null comment '任务名称',
    job_group         varchar(200) not null comment '任务组名',
    description       varchar(250) null comment '相关介绍',
    job_class_name    varchar(250) not null comment '执行任务类名称',
    is_durable        varchar(1)   not null comment '是否持久化',
    is_nonconcurrent  varchar(1)   not null comment '是否并发',
    is_update_data    varchar(1)   not null comment '是否更新数据',
    requests_recovery varchar(1)   not null comment '是否接受恢复执行',
    job_data          blob         null comment '存放持久化job对象',
    primary key (sched_name, job_name, job_group)
)
    comment '任务详细信息表';

create table qrtz_locks
(
    sched_name varchar(120) not null comment '调度名称',
    lock_name  varchar(40)  not null comment '悲观锁名称',
    primary key (sched_name, lock_name)
)
    comment '存储的悲观锁信息表';

create table qrtz_paused_trigger_grps
(
    sched_name    varchar(120) not null comment '调度名称',
    trigger_group varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    primary key (sched_name, trigger_group)
)
    comment '暂停的触发器表';

create table qrtz_scheduler_state
(
    sched_name        varchar(120) not null comment '调度名称',
    instance_name     varchar(200) not null comment '实例名称',
    last_checkin_time bigint       not null comment '上次检查时间',
    checkin_interval  bigint       not null comment '检查间隔时间',
    primary key (sched_name, instance_name)
)
    comment '调度器状态表';

create table qrtz_triggers
(
    sched_name     varchar(120) not null comment '调度名称',
    trigger_name   varchar(200) not null comment '触发器的名字',
    trigger_group  varchar(200) not null comment '触发器所属组的名字',
    job_name       varchar(200) not null comment 'qrtz_job_details表job_name的外键',
    job_group      varchar(200) not null comment 'qrtz_job_details表job_group的外键',
    description    varchar(250) null comment '相关介绍',
    next_fire_time bigint       null comment '上一次触发时间（毫秒）',
    prev_fire_time bigint       null comment '下一次触发时间（默认为-1表示不触发）',
    priority       int          null comment '优先级',
    trigger_state  varchar(16)  not null comment '触发器状态',
    trigger_type   varchar(8)   not null comment '触发器的类型',
    start_time     bigint       not null comment '开始时间',
    end_time       bigint       null comment '结束时间',
    calendar_name  varchar(200) null comment '日程表名称',
    misfire_instr  smallint     null comment '补偿执行的策略',
    job_data       blob         null comment '存放持久化job对象',
    primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_triggers_ibfk_1
        foreign key (sched_name, job_name, job_group) references qrtz_job_details (sched_name, job_name, job_group)
)
    comment '触发器详细信息表';

create table qrtz_blob_triggers
(
    sched_name    varchar(120) not null comment '调度名称',
    trigger_name  varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    blob_data     blob         null comment '存放持久化Trigger对象',
    primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_blob_triggers_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers (sched_name, trigger_name, trigger_group)
)
    comment 'Blob类型的触发器表';

create table qrtz_cron_triggers
(
    sched_name      varchar(120) not null comment '调度名称',
    trigger_name    varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group   varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    cron_expression varchar(200) not null comment 'cron表达式',
    time_zone_id    varchar(80)  null comment '时区',
    primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_cron_triggers_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers (sched_name, trigger_name, trigger_group)
)
    comment 'Cron类型的触发器表';

create table qrtz_simple_triggers
(
    sched_name      varchar(120) not null comment '调度名称',
    trigger_name    varchar(200) not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group   varchar(200) not null comment 'qrtz_triggers表trigger_group的外键',
    repeat_count    bigint       not null comment '重复的次数统计',
    repeat_interval bigint       not null comment '重复的间隔时间',
    times_triggered bigint       not null comment '已经触发的次数',
    primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_simple_triggers_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers (sched_name, trigger_name, trigger_group)
)
    comment '简单触发器的信息表';

create table qrtz_simprop_triggers
(
    sched_name    varchar(120)   not null comment '调度名称',
    trigger_name  varchar(200)   not null comment 'qrtz_triggers表trigger_name的外键',
    trigger_group varchar(200)   not null comment 'qrtz_triggers表trigger_group的外键',
    str_prop_1    varchar(512)   null comment 'String类型的trigger的第一个参数',
    str_prop_2    varchar(512)   null comment 'String类型的trigger的第二个参数',
    str_prop_3    varchar(512)   null comment 'String类型的trigger的第三个参数',
    int_prop_1    int            null comment 'int类型的trigger的第一个参数',
    int_prop_2    int            null comment 'int类型的trigger的第二个参数',
    long_prop_1   bigint         null comment 'long类型的trigger的第一个参数',
    long_prop_2   bigint         null comment 'long类型的trigger的第二个参数',
    dec_prop_1    decimal(13, 4) null comment 'decimal类型的trigger的第一个参数',
    dec_prop_2    decimal(13, 4) null comment 'decimal类型的trigger的第二个参数',
    bool_prop_1   varchar(1)     null comment 'Boolean类型的trigger的第一个参数',
    bool_prop_2   varchar(1)     null comment 'Boolean类型的trigger的第二个参数',
    primary key (sched_name, trigger_name, trigger_group),
    constraint qrtz_simprop_triggers_ibfk_1
        foreign key (sched_name, trigger_name, trigger_group) references qrtz_triggers (sched_name, trigger_name, trigger_group)
)
    comment '同步机制的行锁表';

create index sched_name
    on qrtz_triggers (sched_name, job_name, job_group);

create table quan_bu_ba_biao_xin_xi
(
    `Gene Entrez ID` double null,
    Symbol           text   null,
    `Uniprot ID`     text   null,
    EnsemblID        text   null,
    description      text   null,
    type_of_gene     text   null,
    TCM_TarID        text   null
);

create table syndrome_gene_rel
(
    id              int auto_increment
        primary key,
    tcm_syndrome_id varchar(50)                         not null comment '证候ID',
    tcm_tar_id      varchar(50)                         not null comment '靶标ID',
    source          varchar(100)                        null comment '数据来源',
    created_at      timestamp default CURRENT_TIMESTAMP null,
    constraint uk_syn_gene
        unique (tcm_syndrome_id, tcm_tar_id)
)
    comment '证候-基因关联表' collate = utf8mb4_unicode_ci;

create index idx_syndrome
    on syndrome_gene_rel (tcm_syndrome_id);

create index idx_target
    on syndrome_gene_rel (tcm_tar_id);

create table syndrome_symptom_rel
(
    id              int auto_increment
        primary key,
    tcm_syndrome_id varchar(50)                         not null comment '证候ID',
    tcm_symptom_id  varchar(50)                         not null comment '中医症状ID',
    created_at      timestamp default CURRENT_TIMESTAMP null,
    constraint uk_syn_sym
        unique (tcm_syndrome_id, tcm_symptom_id)
)
    comment '证候-中医症状关联表' collate = utf8mb4_unicode_ci;

create index idx_symptom
    on syndrome_symptom_rel (tcm_symptom_id);

create index idx_syndrome
    on syndrome_symptom_rel (tcm_syndrome_id);

create table sys_config
(
    config_id    int auto_increment comment '参数主键'
        primary key,
    config_name  varchar(100) default ''  null comment '参数名称',
    config_key   varchar(100) default ''  null comment '参数键名',
    config_value varchar(500) default ''  null comment '参数键值',
    config_type  char         default 'N' null comment '系统内置（Y是 N否）',
    create_by    varchar(64)  default ''  null comment '创建者',
    create_time  datetime                 null comment '创建时间',
    update_by    varchar(64)  default ''  null comment '更新者',
    update_time  datetime                 null comment '更新时间',
    remark       varchar(500)             null comment '备注'
)
    comment '参数配置表';

create table sys_dept
(
    dept_id     bigint auto_increment comment '部门id'
        primary key,
    parent_id   bigint      default 0   null comment '父部门id',
    ancestors   varchar(50) default ''  null comment '祖级列表',
    dept_name   varchar(30) default ''  null comment '部门名称',
    order_num   int         default 0   null comment '显示顺序',
    leader      varchar(20)             null comment '负责人',
    phone       varchar(11)             null comment '联系电话',
    email       varchar(50)             null comment '邮箱',
    status      char        default '0' null comment '部门状态（0正常 1停用）',
    del_flag    char        default '0' null comment '删除标志（0代表存在 2代表删除）',
    create_by   varchar(64) default ''  null comment '创建者',
    create_time datetime                null comment '创建时间',
    update_by   varchar(64) default ''  null comment '更新者',
    update_time datetime                null comment '更新时间'
)
    comment '部门表';

create table sys_dict_data
(
    dict_code   bigint auto_increment comment '字典编码'
        primary key,
    dict_sort   int          default 0   null comment '字典排序',
    dict_label  varchar(100) default ''  null comment '字典标签',
    dict_value  varchar(100) default ''  null comment '字典键值',
    dict_type   varchar(100) default ''  null comment '字典类型',
    css_class   varchar(100)             null comment '样式属性（其他样式扩展）',
    list_class  varchar(100)             null comment '表格回显样式',
    is_default  char         default 'N' null comment '是否默认（Y是 N否）',
    status      char         default '0' null comment '状态（0正常 1停用）',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500)             null comment '备注'
)
    comment '字典数据表';

create table sys_dict_type
(
    dict_id     bigint auto_increment comment '字典主键'
        primary key,
    dict_name   varchar(100) default ''  null comment '字典名称',
    dict_type   varchar(100) default ''  null comment '字典类型',
    status      char         default '0' null comment '状态（0正常 1停用）',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500)             null comment '备注',
    constraint dict_type
        unique (dict_type)
)
    comment '字典类型表';

create table sys_job
(
    job_id          bigint auto_increment comment '任务ID',
    job_name        varchar(64)  default ''        not null comment '任务名称',
    job_group       varchar(64)  default 'DEFAULT' not null comment '任务组名',
    invoke_target   varchar(500)                   not null comment '调用目标字符串',
    cron_expression varchar(255) default ''        null comment 'cron执行表达式',
    misfire_policy  varchar(20)  default '3'       null comment '计划执行错误策略（1立即执行 2执行一次 3放弃执行）',
    concurrent      char         default '1'       null comment '是否并发执行（0允许 1禁止）',
    status          char         default '0'       null comment '状态（0正常 1暂停）',
    create_by       varchar(64)  default ''        null comment '创建者',
    create_time     datetime                       null comment '创建时间',
    update_by       varchar(64)  default ''        null comment '更新者',
    update_time     datetime                       null comment '更新时间',
    remark          varchar(500) default ''        null comment '备注信息',
    primary key (job_id, job_name, job_group)
)
    comment '定时任务调度表';

create table sys_job_log
(
    job_log_id     bigint auto_increment comment '任务日志ID'
        primary key,
    job_name       varchar(64)               not null comment '任务名称',
    job_group      varchar(64)               not null comment '任务组名',
    invoke_target  varchar(500)              not null comment '调用目标字符串',
    job_message    varchar(500)              null comment '日志信息',
    status         char          default '0' null comment '执行状态（0正常 1失败）',
    exception_info varchar(2000) default ''  null comment '异常信息',
    create_time    datetime                  null comment '创建时间'
)
    comment '定时任务调度日志表';

create table sys_logininfor
(
    info_id        bigint auto_increment comment '访问ID'
        primary key,
    user_name      varchar(50)  default ''  null comment '用户账号',
    ipaddr         varchar(128) default ''  null comment '登录IP地址',
    login_location varchar(255) default ''  null comment '登录地点',
    browser        varchar(50)  default ''  null comment '浏览器类型',
    os             varchar(50)  default ''  null comment '操作系统',
    status         char         default '0' null comment '登录状态（0成功 1失败）',
    msg            varchar(255) default ''  null comment '提示消息',
    login_time     datetime                 null comment '访问时间'
)
    comment '系统访问记录';

create index idx_sys_logininfor_lt
    on sys_logininfor (login_time);

create index idx_sys_logininfor_s
    on sys_logininfor (status);

create table sys_menu
(
    menu_id     bigint auto_increment comment '菜单ID'
        primary key,
    menu_name   varchar(50)              not null comment '菜单名称',
    parent_id   bigint       default 0   null comment '父菜单ID',
    order_num   int          default 0   null comment '显示顺序',
    path        varchar(200) default ''  null comment '路由地址',
    component   varchar(255)             null comment '组件路径',
    query       varchar(255)             null comment '路由参数',
    route_name  varchar(50)  default ''  null comment '路由名称',
    is_frame    int          default 1   null comment '是否为外链（0是 1否）',
    is_cache    int          default 0   null comment '是否缓存（0缓存 1不缓存）',
    menu_type   char         default ''  null comment '菜单类型（M目录 C菜单 F按钮）',
    visible     char         default '0' null comment '菜单状态（0显示 1隐藏）',
    status      char         default '0' null comment '菜单状态（0正常 1停用）',
    perms       varchar(100)             null comment '权限标识',
    icon        varchar(100) default '#' null comment '菜单图标',
    create_by   varchar(64)  default ''  null comment '创建者',
    create_time datetime                 null comment '创建时间',
    update_by   varchar(64)  default ''  null comment '更新者',
    update_time datetime                 null comment '更新时间',
    remark      varchar(500) default ''  null comment '备注'
)
    comment '菜单权限表';

create table sys_notice
(
    notice_id      int auto_increment comment '公告ID'
        primary key,
    notice_title   varchar(50)             not null comment '公告标题',
    notice_type    char                    not null comment '公告类型（1通知 2公告）',
    notice_content longblob                null comment '公告内容',
    status         char        default '0' null comment '公告状态（0正常 1关闭）',
    create_by      varchar(64) default ''  null comment '创建者',
    create_time    datetime                null comment '创建时间',
    update_by      varchar(64) default ''  null comment '更新者',
    update_time    datetime                null comment '更新时间',
    remark         varchar(255)            null comment '备注'
)
    comment '通知公告表';

create table sys_oper_log
(
    oper_id        bigint auto_increment comment '日志主键'
        primary key,
    title          varchar(50)   default '' null comment '模块标题',
    business_type  int           default 0  null comment '业务类型（0其它 1新增 2修改 3删除）',
    method         varchar(200)  default '' null comment '方法名称',
    request_method varchar(10)   default '' null comment '请求方式',
    operator_type  int           default 0  null comment '操作类别（0其它 1后台用户 2手机端用户）',
    oper_name      varchar(50)   default '' null comment '操作人员',
    dept_name      varchar(50)   default '' null comment '部门名称',
    oper_url       varchar(255)  default '' null comment '请求URL',
    oper_ip        varchar(128)  default '' null comment '主机地址',
    oper_location  varchar(255)  default '' null comment '操作地点',
    oper_param     varchar(2000) default '' null comment '请求参数',
    json_result    varchar(2000) default '' null comment '返回参数',
    status         int           default 0  null comment '操作状态（0正常 1异常）',
    error_msg      varchar(2000) default '' null comment '错误消息',
    oper_time      datetime                 null comment '操作时间',
    cost_time      bigint        default 0  null comment '消耗时间'
)
    comment '操作日志记录';

create index idx_sys_oper_log_bt
    on sys_oper_log (business_type);

create index idx_sys_oper_log_ot
    on sys_oper_log (oper_time);

create index idx_sys_oper_log_s
    on sys_oper_log (status);

create table sys_post
(
    post_id     bigint auto_increment comment '岗位ID'
        primary key,
    post_code   varchar(64)            not null comment '岗位编码',
    post_name   varchar(50)            not null comment '岗位名称',
    post_sort   int                    not null comment '显示顺序',
    status      char                   not null comment '状态（0正常 1停用）',
    create_by   varchar(64) default '' null comment '创建者',
    create_time datetime               null comment '创建时间',
    update_by   varchar(64) default '' null comment '更新者',
    update_time datetime               null comment '更新时间',
    remark      varchar(500)           null comment '备注'
)
    comment '岗位信息表';

create table sys_role
(
    role_id             bigint auto_increment comment '角色ID'
        primary key,
    role_name           varchar(30)             not null comment '角色名称',
    role_key            varchar(100)            not null comment '角色权限字符串',
    role_sort           int                     not null comment '显示顺序',
    data_scope          char        default '1' null comment '数据范围（1：全部数据权限 2：自定数据权限 3：本部门数据权限 4：本部门及以下数据权限）',
    menu_check_strictly tinyint(1)  default 1   null comment '菜单树选择项是否关联显示',
    dept_check_strictly tinyint(1)  default 1   null comment '部门树选择项是否关联显示',
    status              char                    not null comment '角色状态（0正常 1停用）',
    del_flag            char        default '0' null comment '删除标志（0代表存在 2代表删除）',
    create_by           varchar(64) default ''  null comment '创建者',
    create_time         datetime                null comment '创建时间',
    update_by           varchar(64) default ''  null comment '更新者',
    update_time         datetime                null comment '更新时间',
    remark              varchar(500)            null comment '备注'
)
    comment '角色信息表';

create table sys_role_dept
(
    role_id bigint not null comment '角色ID',
    dept_id bigint not null comment '部门ID',
    primary key (role_id, dept_id)
)
    comment '角色和部门关联表';

create table sys_role_menu
(
    role_id bigint not null comment '角色ID',
    menu_id bigint not null comment '菜单ID',
    primary key (role_id, menu_id)
)
    comment '角色和菜单关联表';

create table sys_user
(
    user_id         bigint auto_increment comment '用户ID'
        primary key,
    dept_id         bigint                    null comment '部门ID',
    user_name       varchar(30)               not null comment '用户账号',
    nick_name       varchar(30)               not null comment '用户昵称',
    user_type       varchar(2)   default '00' null comment '用户类型（00系统用户）',
    email           varchar(50)  default ''   null comment '用户邮箱',
    phonenumber     varchar(11)  default ''   null comment '手机号码',
    sex             char         default '0'  null comment '用户性别（0男 1女 2未知）',
    avatar          varchar(100) default ''   null comment '头像地址',
    password        varchar(100) default ''   null comment '密码',
    status          char         default '0'  null comment '账号状态（0正常 1停用）',
    del_flag        char         default '0'  null comment '删除标志（0代表存在 2代表删除）',
    login_ip        varchar(128) default ''   null comment '最后登录IP',
    login_date      datetime                  null comment '最后登录时间',
    pwd_update_date datetime                  null comment '密码最后更新时间',
    create_by       varchar(64)  default ''   null comment '创建者',
    create_time     datetime                  null comment '创建时间',
    update_by       varchar(64)  default ''   null comment '更新者',
    update_time     datetime                  null comment '更新时间',
    remark          varchar(500)              null comment '备注'
)
    comment '用户信息表';

create table sys_user_post
(
    user_id bigint not null comment '用户ID',
    post_id bigint not null comment '岗位ID',
    primary key (user_id, post_id)
)
    comment '用户与岗位关联表';

create table sys_user_role
(
    user_id bigint not null comment '用户ID',
    role_id bigint not null comment '角色ID',
    primary key (user_id, role_id)
)
    comment '用户和角色关联表';

create table targets
(
    id             int auto_increment
        primary key,
    tcm_tar_id     varchar(50)                         not null comment '靶标ID (Target_1)',
    gene_entrez_id int                                 null,
    symbol         varchar(100)                        null,
    uniprot_id     varchar(50)                         null comment 'Uniprot ID',
    ensembl_id     varchar(50)                         null comment 'Ensembl ID',
    description    text                                null comment '基因描述',
    type_of_gene   varchar(100)                        null comment '基因类型',
    created_at     timestamp default CURRENT_TIMESTAMP null,
    updated_at     timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint idx_entrez
        unique (gene_entrez_id),
    constraint tcm_tar_id
        unique (tcm_tar_id)
)
    comment '靶标/基因信息表' collate = utf8mb4_unicode_ci;

create index idx_symbol
    on targets (symbol);

create index idx_uniprot
    on targets (uniprot_id);

create table tcm_compounds
(
    id                int auto_increment
        primary key,
    inchikey          varchar(100)                        not null comment 'InChIKey唯一标识',
    canonical_smiles  text                                null comment '标准SMILES',
    pubchem_cid       bigint                              null comment 'PubChem CID',
    molecular_formula varchar(200)                        null comment '分子式',
    molecular_weight  double                              null comment '分子量',
    exact_mass        double                              null comment '精确质量',
    heavy_atom_count  int                                 null comment '重原子数',
    num_atoms         int                                 null comment '原子总数',
    num_bonds         int                                 null comment '键的数量',
    num_rings         int                                 null comment '环的数量',
    h_bond_donors     int                                 null comment '氢键供体',
    h_bond_acceptors  int                                 null comment '氢键受体',
    logp              double                              null comment 'LogP值',
    tpsa              double                              null comment '拓扑极性表面积',
    rotatable_bonds   int                                 null comment '可旋转键',
    fraction_csp3     double                              null comment 'Csp3分数',
    created_at        timestamp default CURRENT_TIMESTAMP null,
    updated_at        timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    compound_name     varchar(255)                        null,
    constraint inchikey
        unique (inchikey)
)
    comment '中药化合物理化性质表' collate = utf8mb4_unicode_ci;

create index idx_molecular_weight
    on tcm_compounds (molecular_weight);

create index idx_pubchem
    on tcm_compounds (pubchem_cid);

create index idx_tcm_compounds_molecular_formula
    on tcm_compounds (molecular_formula);

create table tcm_prescriptions
(
    id                  int auto_increment
        primary key,
    tcm_prescription_id varchar(50)                         not null comment '方剂ID (TCMSSD59734)',
    name_zh             varchar(255)                        null,
    pinyin_name         varchar(255)                        null comment '拼音名称',
    source              text                                null comment '来源',
    indications_zh      text                                null comment '主治（中文）',
    indications_en      text                                null comment '主治（英文）',
    effects             text                                null comment '功效（英文）',
    effects_zh          text                                null comment '功效（中文）',
    created_at          timestamp default CURRENT_TIMESTAMP null,
    updated_at          timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP
)
    comment 'TCM方剂（含中成药）信息表' collate = utf8mb4_unicode_ci;

create fulltext index ft_indications
    on tcm_prescriptions (indications_zh);

create index idx_pinyin
    on tcm_prescriptions (pinyin_name(100));

create index idx_prescription_name
    on tcm_prescriptions (name_zh(100));

create table tcm_symptoms
(
    id                 int auto_increment
        primary key,
    tcm_symptom_id     varchar(50)                         not null comment '中医症状ID (TCM_Symptom1)',
    symptom_name_zh    varchar(255)                        not null comment '症状名称（中文）',
    symptom_pinyin     varchar(255)                        null comment '拼音',
    symptom_definition text                                null comment '症状定义',
    symptom_locus      varchar(100)                        null comment '症状部位',
    symptom_property   varchar(255)                        null comment '症状属性',
    type               varchar(100)                        null comment '类型 (Ontological/Synonymous)',
    created_at         timestamp default CURRENT_TIMESTAMP null,
    updated_at         timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint tcm_symptom_id
        unique (tcm_symptom_id)
)
    comment '中医症状信息表' collate = utf8mb4_unicode_ci;

create fulltext index ft_definition
    on tcm_symptoms (symptom_definition);

create index idx_locus
    on tcm_symptoms (symptom_locus);

create index idx_symptom_name
    on tcm_symptoms (symptom_name_zh);

create table tcm_syndromes
(
    id                      int auto_increment
        primary key,
    tcm_syndrome_id         varchar(50)                         not null comment '证候ID (TCM_Syndrome1)',
    syndrome_name_zh        varchar(255)                        not null comment '证候名称（中文）',
    syndrome_english        varchar(500)                        null comment '证候名称（英文）',
    syndrome_pinyin         varchar(255)                        null comment '拼音',
    syndrome_definition_zh  text                                null comment '证候定义（中文）',
    syndrome_description_en text                                null comment '证候描述（英文）',
    category_zh             varchar(255)                        null comment '分类（中文）',
    category_en             varchar(255)                        null comment '分类（英文）',
    source                  varchar(100)                        null comment '数据来源',
    created_at              timestamp default CURRENT_TIMESTAMP null,
    updated_at              timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint tcm_syndrome_id
        unique (tcm_syndrome_id)
)
    comment '中医证候信息表' collate = utf8mb4_unicode_ci;

create fulltext index ft_definition
    on tcm_syndromes (syndrome_definition_zh);

create index idx_category
    on tcm_syndromes (category_zh);

create index idx_syndrome_name
    on tcm_syndromes (syndrome_name_zh);

create table tcm_wm_symptom_rel
(
    id             int auto_increment
        primary key,
    tcm_symptom_id varchar(50)                         not null comment '中医症状ID',
    wm_symptom_id  varchar(50)                         not null comment '西医症状ID',
    created_at     timestamp default CURRENT_TIMESTAMP null,
    constraint uk_tcm_wm_sym
        unique (tcm_symptom_id, wm_symptom_id)
)
    comment '中医症状-西医症状关联表' collate = utf8mb4_unicode_ci;

create index idx_tcm_symptom
    on tcm_wm_symptom_rel (tcm_symptom_id);

create index idx_wm_symptom
    on tcm_wm_symptom_rel (wm_symptom_id);

create table user_web
(
    id          int auto_increment comment '自增主键ID'
        primary key,
    username    varchar(50)                        not null comment '用户名',
    account     varchar(50)                        not null comment '账号',
    password    varchar(100)                       not null comment '密码',
    email       varchar(100)                       not null comment '邮箱',
    create_time datetime default CURRENT_TIMESTAMP not null comment '创建时间',
    update_time datetime default CURRENT_TIMESTAMP not null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uniq_account
        unique (account),
    constraint uniq_email
        unique (email)
)
    comment '用户信息表';

create table wm_symptom_gene_rel
(
    id            int auto_increment
        primary key,
    wm_symptom_id varchar(50)                         not null comment '西医症状ID',
    tcm_tar_id    varchar(50)                         not null comment '靶标ID',
    created_at    timestamp default CURRENT_TIMESTAMP null,
    constraint uk_wm_sym_gene
        unique (wm_symptom_id, tcm_tar_id)
)
    comment '西医症状-基因关联表' collate = utf8mb4_unicode_ci;

create index idx_target
    on wm_symptom_gene_rel (tcm_tar_id);

create index idx_wm_symptom
    on wm_symptom_gene_rel (wm_symptom_id);

create table wm_symptoms
(
    id            int auto_increment
        primary key,
    wm_symptom_id varchar(50)                         null,
    symptom_name  varchar(255)                        null,
    umls_id       varchar(50)                         null comment 'UMLS标识符',
    created_at    timestamp default CURRENT_TIMESTAMP null,
    updated_at    timestamp default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP,
    constraint wm_symptom_id
        unique (wm_symptom_id)
)
    comment '西医症状信息表' collate = utf8mb4_unicode_ci;

create index idx_umls
    on wm_symptoms (umls_id);

create index idx_wm_symptom_name
    on wm_symptoms (symptom_name);

create definer = root@localhost view v_disease_target_herb as
select distinct `d`.`disease_id`   AS `disease_id`,
                `d`.`disease_name` AS `disease_name`,
                `dg`.`tcm_tar_id`  AS `tcm_tar_id`,
                `t`.`symbol`       AS `target_symbol`,
                `ct`.`inchikey`    AS `inchikey`,
                `hc`.`tcm_herb_id` AS `tcm_herb_id`,
                `h`.`herb_name_zh` AS `herb_name_zh`
from (((((`ruoyi_vue_demo`.`diseases` `d` join `ruoyi_vue_demo`.`disease_gene_rel` `dg`
          on ((`d`.`disease_id` = `dg`.`disease_id`))) join `ruoyi_vue_demo`.`targets` `t`
         on ((`dg`.`tcm_tar_id` = `t`.`tcm_tar_id`))) join `ruoyi_vue_demo`.`compound_target_rel` `ct`
        on ((`t`.`tcm_tar_id` = `ct`.`tcm_tar_id`))) join `ruoyi_vue_demo`.`herb_compound_rel` `hc`
       on ((`ct`.`inchikey` = `hc`.`inchikey`))) join `ruoyi_vue_demo`.`core_tcm_herbs` `h`
      on ((`hc`.`tcm_herb_id` = `h`.`tcm_herb_id`)));

-- comment on column v_disease_target_herb.disease_id not supported: 疾病ID (DOID:11832)

-- comment on column v_disease_target_herb.tcm_tar_id not supported: 靶标ID

-- comment on column v_disease_target_herb.inchikey not supported: 化合物InChIKey

-- comment on column v_disease_target_herb.tcm_herb_id not supported: 中药ID

-- comment on column v_disease_target_herb.herb_name_zh not supported: 中药名称（中文）

create definer = root@localhost view v_herb_compound_target as
select `h`.`tcm_herb_id`       AS `tcm_herb_id`,
       `h`.`herb_name_zh`      AS `herb_name_zh`,
       `hc`.`inchikey`         AS `inchikey`,
       `c`.`molecular_formula` AS `molecular_formula`,
       `c`.`molecular_weight`  AS `molecular_weight`,
       `ct`.`tcm_tar_id`       AS `tcm_tar_id`,
       `t`.`symbol`            AS `target_symbol`,
       `t`.`description`       AS `target_description`
from ((((`ruoyi_vue_demo`.`core_tcm_herbs` `h` join `ruoyi_vue_demo`.`herb_compound_rel` `hc`
         on ((`h`.`tcm_herb_id` = `hc`.`tcm_herb_id`))) join `ruoyi_vue_demo`.`tcm_compounds` `c`
        on ((`hc`.`inchikey` = `c`.`inchikey`))) join `ruoyi_vue_demo`.`compound_target_rel` `ct`
       on ((`c`.`inchikey` = `ct`.`inchikey`))) join `ruoyi_vue_demo`.`targets` `t`
      on ((`ct`.`tcm_tar_id` = `t`.`tcm_tar_id`)));

-- comment on column v_herb_compound_target.tcm_herb_id not supported: 中药唯一标识ID (HERB_1)

-- comment on column v_herb_compound_target.herb_name_zh not supported: 中药名称（中文）

-- comment on column v_herb_compound_target.inchikey not supported: 化合物InChIKey

-- comment on column v_herb_compound_target.molecular_formula not supported: 分子式

-- comment on column v_herb_compound_target.molecular_weight not supported: 分子量

-- comment on column v_herb_compound_target.tcm_tar_id not supported: 靶标ID

-- comment on column v_herb_compound_target.target_description not supported: 基因描述

create definer = root@localhost view v_prescription_herbs as
select `p`.`tcm_prescription_id` AS `tcm_prescription_id`,
       `p`.`name_zh`             AS `prescription_name`,
       `h`.`tcm_herb_id`         AS `tcm_herb_id`,
       `h`.`herb_name_zh`        AS `herb_name_zh`,
       'core'                    AS `herb_type`
from ((`ruoyi_vue_demo`.`tcm_prescriptions` `p` join `ruoyi_vue_demo`.`prescription_core_herb_rel` `pc`
       on ((`p`.`tcm_prescription_id` = `pc`.`tcm_prescription_id`))) join `ruoyi_vue_demo`.`core_tcm_herbs` `h`
      on ((`pc`.`tcm_herb_id` = `h`.`tcm_herb_id`)))
union all
select `p`.`tcm_prescription_id` AS `tcm_prescription_id`,
       `p`.`name_zh`             AS `prescription_name`,
       `o`.`tcm_herb2_id`        AS `tcm_herb_id`,
       `o`.`herb_name_zh`        AS `herb_name_zh`,
       'other'                   AS `herb_type`
from ((`ruoyi_vue_demo`.`tcm_prescriptions` `p` join `ruoyi_vue_demo`.`prescription_other_herb_rel` `po`
       on ((`p`.`tcm_prescription_id` = `po`.`tcm_prescription_id`))) join `ruoyi_vue_demo`.`other_tcm_herbs` `o`
      on ((`po`.`tcm_herb2_id` = `o`.`tcm_herb2_id`)));
