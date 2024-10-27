package com.rudniev.hackentesttask.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity(name = "load_state")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoadState {
    @Id
    private int id;
    private long lastProcessedBlock;
}
