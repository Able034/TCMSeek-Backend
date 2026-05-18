create table public.compound_structures
(
    inchikey          varchar(27) not null
        primary key,
    mol               mol,
    mfp2              bfp,
    smiles            text,
    molecular_formula varchar(100),
    molecular_weight  numeric(10, 4),
    created_at        timestamp default CURRENT_TIMESTAMP
);

alter table public.compound_structures
    owner to postgres;

create index idx_compound_structures_mol_gist
    on public.compound_structures using gist (mol);

create index idx_compound_structures_mfp2_gist
    on public.compound_structures using gist (mfp2);
