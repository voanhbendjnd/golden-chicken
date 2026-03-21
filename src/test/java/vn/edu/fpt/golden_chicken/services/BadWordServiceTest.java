package vn.edu.fpt.golden_chicken.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;
import org.apache.kafka.common.errors.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import vn.edu.fpt.golden_chicken.domain.entity.BadWord;
import vn.edu.fpt.golden_chicken.domain.request.BadWordDTO;
import vn.edu.fpt.golden_chicken.repositories.BadWordRepository;
import vn.edu.fpt.golden_chicken.utils.BadWordFilterUtility;
import vn.edu.fpt.golden_chicken.utils.exceptions.DataInvalidException;

@ExtendWith(MockitoExtension.class)
class BadWordServiceTest {

    @Mock
    BadWordRepository badWordRepository;

    @Mock
    KafkaTemplate<String, String> kafkaScanOldReview;

    @Mock
    BadWordFilterUtility badWordFilterUtility;

    @InjectMocks
    BadWordService badWordService;

    private BadWordDTO badWordDTO;
    private BadWord badWord;

    @BeforeEach
    void setUp() {
        badWordDTO = new BadWordDTO();
        badWordDTO.setId(1L);
        badWordDTO.setWord("testword");
        badWordDTO.setStatus(true);
        badWordDTO.setApplyFromNowOn(true);

        badWord = new BadWord();
        badWord.setId(1L);
        badWord.setWord("testword");
        badWord.setStatus(true);
    }

    @Test
    void create_Success() {
        when(badWordRepository.existsByWordIgnoreCase(anyString())).thenReturn(false);
        when(badWordRepository.save(any(BadWord.class))).thenReturn(badWord);

        badWordService.create(badWordDTO);

        verify(badWordRepository, times(1)).save(any(BadWord.class));
        verify(badWordFilterUtility, times(1)).loadAndCompileBadWords();
        verify(kafkaScanOldReview, times(1)).send(eq("scan-old-reviews-topic"), eq("testword"));
    }

    @Test
    void create_DuplicateWord_ThrowsException() {
        when(badWordRepository.existsByWordIgnoreCase(anyString())).thenReturn(true);

        assertThrows(DataInvalidException.class, () -> badWordService.create(badWordDTO));
        verify(badWordRepository, never()).save(any(BadWord.class));
    }

    @Test
    void update_Success() {
        when(badWordRepository.existsByWordIgnoreCaseAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(badWordRepository.findById(anyLong())).thenReturn(Optional.of(badWord));
        when(badWordRepository.save(any(BadWord.class))).thenReturn(badWord);

        badWordService.update(badWordDTO);

        verify(badWordRepository, times(1)).save(any(BadWord.class));
        verify(badWordFilterUtility, times(1)).loadAndCompileBadWords();
    }

    @Test
    void update_DuplicateWord_ThrowsException() {
        when(badWordRepository.existsByWordIgnoreCaseAndIdNot(anyString(), anyLong())).thenReturn(true);

        assertThrows(DataInvalidException.class, () -> badWordService.update(badWordDTO));
    }

    @Test
    void update_NotFound_ThrowsException() {
        when(badWordRepository.existsByWordIgnoreCaseAndIdNot(anyString(), anyLong())).thenReturn(false);
        when(badWordRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> badWordService.update(badWordDTO));
    }

    @Test
    void delete_Success() {
        when(badWordRepository.findById(anyLong())).thenReturn(Optional.of(badWord));
        
        badWordService.delete(1L, true);

        verify(badWordRepository, times(1)).delete(badWord);
        verify(badWordFilterUtility, times(1)).loadAndCompileBadWords();
    }

    @Test
    void delete_NotFound_ThrowsException() {
        when(badWordRepository.findById(anyLong())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> badWordService.delete(1L, true));
    }

    @Test
    void revertStatus_Success() {
        when(badWordRepository.findById(anyLong())).thenReturn(Optional.of(badWord));
        when(badWordRepository.save(any(BadWord.class))).thenReturn(badWord);

        badWordService.revertStatus(1L, true);

        assertNotNull(badWord.getStatus());
        verify(badWordRepository, times(1)).save(badWord);
        verify(kafkaScanOldReview, times(1)).send(anyString(), anyString());
    }
}
