package com.canabackend.cana.controllers;

import com.canabackend.cana.dtos.GetItemsExistentesDto;
import com.canabackend.cana.models.ItemsCana;
import com.canabackend.cana.services.ItemsCanaSvc;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/items")
public class ItemsCanaController {
    @Autowired
    private ItemsCanaSvc service;

    @GetMapping("/privado/listarItems")
    public ResponseEntity<List<GetItemsExistentesDto>> listarItems() {
        return ResponseEntity.ok(this.service.getItemsExistentes());
    }

    @PostMapping("/privado/guardarItem")
    public ResponseEntity<ItemsCana> crearItem(@RequestBody ItemsCana itemCana) {
        return ResponseEntity.ok(this.service.crearItemsCana(itemCana));
    }

    @PutMapping("/privado/eliminarItem/{id}")
    public ResponseEntity<Void> desactivarItem(@PathVariable Long id) {
       this.service.desactivarItem(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/privado/editarItem/{id}")
    public ResponseEntity<Void> modificarItem(@RequestBody ItemsCana itemCana,  @PathVariable Long id) {
        this.service.modificarItem(itemCana,id);
        return ResponseEntity.noContent().build();
    }
}
