package guru.springframework.sfgpetclinic.controllers;

import guru.springframework.sfgpetclinic.model.Owner;
import guru.springframework.sfgpetclinic.services.OwnerService;
import guru.springframework.sfgpetclinic.services.map.OwnerMapService;
import guru.springframework.sfgpetclinic.services.map.PetMapService;
import guru.springframework.sfgpetclinic.services.map.PetTypeMapService;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatchers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class OwnerControllerTest {

    private static final Long OWNER_ID_1 = 1L;
    private static final String OWNER_LAST_NAME_1 = "Smith";
    private static final Long OWNER_ID_2 = 2L;
    private static final String OWNER_LAST_NAME_2 = "Smooth";

    private OwnerMapService ownerMapService;

    @Mock
    OwnerService ownerService;

    @InjectMocks
    OwnerController controller;

    Set<Owner> owners;

    MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ownerMapService = new OwnerMapService(new PetTypeMapService(), new PetMapService());

        ownerMapService.save(Owner.builder().id(OWNER_ID_1).lastName(OWNER_LAST_NAME_1).build());

        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .build();
    }

    @Test
    void findAll() {
        Set<Owner> ownerSet = ownerMapService.findAll();

        assertEquals(1, ownerSet.size());
    }

    @Test
    void findById() {
        Owner owner = ownerMapService.findById(OWNER_ID_1);

        assertEquals(OWNER_ID_1, owner.getId());
    }

    @Test
    void saveExistingId() {
        Owner owner2 = Owner.builder().id(OWNER_ID_2).build();

        Owner savedOwner = ownerMapService.save(owner2);

        assertEquals(OWNER_ID_2, savedOwner.getId());

    }

    @Test
    void saveNoId() {

        Owner savedOwner = ownerMapService.save(Owner.builder().build());

        assertNotNull(savedOwner);
        assertNotNull(savedOwner.getId());
    }

    @Test
    void delete() {
        ownerMapService.delete(ownerMapService.findById(OWNER_ID_1));

        assertEquals(0, ownerMapService.findAll().size());
    }

    @Test
    void deleteById() {
        ownerMapService.deleteById(OWNER_ID_1);

        assertEquals(0, ownerMapService.findAll().size());
    }

    @Test
    void findByLastName() {
        Owner smith = ownerMapService.findByLastName(OWNER_LAST_NAME_1);

        assertNotNull(smith);

        assertEquals(OWNER_ID_1, smith.getId());

    }

    @Test
    void findByLastNameNotFound() {
        Owner smith = ownerMapService.findByLastName("foo");

        assertNull(smith);
    }


    @Test
    void findByLastNameLikeInMiddleName() {
        final var owners = ownerMapService.findAllByLastNameLike("th");

        assertNotNull(owners);
        assertEquals(1, owners.size());
        final var owner = owners.iterator().next();
        assertEquals(OWNER_ID_1, owner.getId());
        assertEquals(OWNER_LAST_NAME_1, owner.getLastName());
    }

    @Test
    void findByLastNameLikeCaseInsensitive() {
        final var owners = ownerMapService.findAllByLastNameLike("smi");

        assertNotNull(owners);
        assertEquals(1, owners.size());
        final var owner = owners.iterator().next();
        assertEquals(OWNER_ID_1, owner.getId());
        assertEquals(OWNER_LAST_NAME_1, owner.getLastName());
    }

    @Test
    void findByLastNameLikeReturnsOne() {
        ownerMapService.save(Owner.builder().id(OWNER_ID_2).lastName("smooth").build());

        final var owners = ownerMapService.findAllByLastNameLike("Smi");

        assertNotNull(owners);
        assertEquals(1, owners.size());
        final var owner = owners.iterator().next();
        assertEquals(OWNER_ID_1, owner.getId());
        assertEquals(OWNER_LAST_NAME_1, owner.getLastName());
    }

    @Test
    void findByLastNameLikeReturnsMany() {
        ownerMapService.save(Owner.builder().id(OWNER_ID_2).lastName(OWNER_LAST_NAME_2).build());

        final var owners = ownerMapService.findAllByLastNameLike("Sm");

        assertNotNull(owners);
        assertEquals(2, owners.size());
    }

    @SneakyThrows
    @Test
    void initCreationForm() {
        mockMvc.perform(get("/owners/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                .andExpect(model().attributeExists("owner"));

        verifyNoInteractions(ownerService);
    }

    @SneakyThrows
    @Test
    void processCreationForm() {
        when(ownerService.save(ArgumentMatchers.any())).thenReturn(Owner.builder().id(1l).build());

        mockMvc.perform(post("/owners/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/1"))
                .andExpect(model().attributeExists("owner"));

        verify(ownerService).save(ArgumentMatchers.any());
    }

    @SneakyThrows
    @Test
    void initUpdateOwnerForm() {
        when(ownerService.findById(anyLong())).thenReturn(Owner.builder().id(1l).build());

        mockMvc.perform(get("/owners/1/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("owners/createOrUpdateOwnerForm"))
                .andExpect(model().attributeExists("owner"));
    }

    @SneakyThrows
    @Test
    void processUpdateOwnerForm() {
        when(ownerService.save(ArgumentMatchers.any())).thenReturn(Owner.builder().id(1l).build());

        mockMvc.perform(post("/owners/1/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(view().name("redirect:/owners/1"))
                .andExpect(model().attributeExists("owner"));

        verify(ownerService).save(ArgumentMatchers.any());
    }
}