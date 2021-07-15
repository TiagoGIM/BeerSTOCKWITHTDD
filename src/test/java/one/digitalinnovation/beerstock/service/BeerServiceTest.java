package one.digitalinnovation.beerstock.service;
import one.digitalinnovation.beerstock.builder.BeerDTOBuilder;
import one.digitalinnovation.beerstock.dto.BeerDTO;
import one.digitalinnovation.beerstock.entity.Beer;
import one.digitalinnovation.beerstock.exception.BeerAlreadyRegisteredException;
import one.digitalinnovation.beerstock.exception.BeerNotFoundException;
import one.digitalinnovation.beerstock.mapper.BeerMapper;
import one.digitalinnovation.beerstock.repository.BeerRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class BeerServiceTest {

    private static final long INVALID_BEER_ID = 1L;

    @Mock
    private BeerRepository beerRepository;
    // faz a conversão de DTOs e Entitys, assim não precisa mockar.
    private  BeerMapper beerMapper = BeerMapper.INSTANCE;

    @InjectMocks
    private BeerService beerService;

    @Test
    void whenBeerInformedThenItShouldBeCreated() throws BeerAlreadyRegisteredException {
      //given
      BeerDTO beerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
      Beer expectedSalvedBeer = beerMapper.toModel(beerDTO);
      //when
      when(beerRepository.findByName(beerDTO.getName()))
              .thenReturn(Optional.empty());
      when(beerRepository.save(expectedSalvedBeer))
              .thenReturn(expectedSalvedBeer);
        //then
      BeerDTO createdBeerDto = beerService.createBeer(beerDTO);
        //Hamcrest
      assertThat(createdBeerDto.getId(), is(equalTo(beerDTO.getId())));
      assertThat(createdBeerDto.getQuantity(),is(greaterThan(3)));
        //mockito
      assertEquals(beerDTO.getName(),createdBeerDto.getName());

    }
    @Test
    void whenAlreadyRegisteredBeerInformedTHenAnExceptionShouldBeThrown() throws BeerAlreadyRegisteredException {
        BeerDTO  expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer duplicatedBeer = beerMapper.toModel(expectedBeerDTO);

        // when
        when(beerRepository.findByName(expectedBeerDTO.getName()))
                .thenReturn(Optional.of(duplicatedBeer));
        //then
        assertThrows(BeerAlreadyRegisteredException.class,
                () -> beerService.createBeer(expectedBeerDTO));
    }
    @Test
    void whenValidBeerNameIsGivenThenReturnABeer() throws BeerNotFoundException {
    // given
        BeerDTO  expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundedBeer = beerMapper.toModel(expectedBeerDTO);
        when(beerRepository.findByName(expectedFoundedBeer.getName()))
                .thenReturn(Optional.of(expectedFoundedBeer));
        //then
        BeerDTO foundedBeerDTO= beerService.findByName(expectedFoundedBeer.getName());
        assertThat(foundedBeerDTO,is(equalTo(expectedBeerDTO)));
    }
    @Test
    void whenNotValidBeerNameIsGivenThenThrowAnError() throws BeerNotFoundException {
        // given
        BeerDTO  expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();

       when(beerRepository.findByName(expectedBeerDTO.getName()))
               .thenReturn(Optional.empty());
        //then
        assertThrows(BeerNotFoundException.class,
                () -> beerService.findByName(expectedBeerDTO.getName()));
    }

    @Test
    void whenLIstBeerIsCalledThenReturnAListOfBeers(){
        // given
        BeerDTO  expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer expectedFoundedBeer = beerMapper.toModel(expectedBeerDTO);
        when(beerRepository.findAll()).thenReturn(Collections.singletonList(expectedFoundedBeer));
        //then
        List<BeerDTO> foundListBeerDTO = beerService.listAll();
        assertThat(foundListBeerDTO, is(not(empty())));
        assertThat(foundListBeerDTO.get(0),is(equalTo(expectedBeerDTO)));

    }
    @Test
    void whenLIstBeerIsCalledThenReturnAEmptyListOfBeers(){
        when(beerRepository.findAll()).thenReturn(Collections.emptyList());
        List<BeerDTO> foundListBeerDTO = beerService.listAll();
        assertThat(foundListBeerDTO, is(empty()));
    }

    @Test
    void whenExclusionIsCalledWithValidIdTheABeerShouldBeDeleted () throws BeerNotFoundException {
        // given
        BeerDTO  expectedBeerDTO = BeerDTOBuilder.builder().build().toBeerDTO();
        Beer foundedBeer = beerMapper.toModel(expectedBeerDTO);
        // when
        when(beerRepository.findById(expectedBeerDTO.getId())).thenReturn(Optional.of(foundedBeer));
        doNothing().when(beerRepository).deleteById(expectedBeerDTO.getId());
        beerService.deleteById(expectedBeerDTO.getId());
        // then
        verify(beerRepository, times(1)).findById(expectedBeerDTO.getId());
        verify(beerRepository, times(1)).deleteById(expectedBeerDTO.getId());
    }

}
