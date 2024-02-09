package pl.pop.interview.master.answer;

import org.junit.Before;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pl.pop.interview.master.practitioner.*;
import pl.pop.interview.master.question.*;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnswerManagerTest {
    private static final String YES = "Yes";
    private static final String NO = "No";
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private QuestionRepository questionRepository;
    @Mock
    private QuestionFacade questionFacade;
    @Mock
    private PractitionerFacade practitionerFacade;
    @InjectMocks
    private AnswerManager answerManager;

    @Test
    public void testFindRandomQuestion() {
        Question question = new Question("content", true);
        QuestionDTO questionDTO2 = new QuestionDTO("no content", false);
        QuestionDTO questionDTO = new QuestionDTO("content", true);

        questionRepository.save(question);

        when(questionRepository.findRandomQuestion()).thenReturn(Optional.of(question));
        when( questionFacade.mapToDto(question)).thenReturn(questionDTO);
        assertSame(questionDTO, questionFacade.findRandomQuestion()); // tu null
        assertNotSame(questionDTO2, questionFacade.findRandomQuestion());
    }

    @Test
    public void testSaveNewCorrectAnswer() {
        Practitioner mockPractitioner = mock(Practitioner.class);
        when(mockPractitioner.getId()).thenReturn( 1L );

        Question question = new Question("content", true);
        question.setId( 1L );

        // just for place in the practitioner answer list for  comparison with question
        Question anotherQuestion = new Question();
        anotherQuestion.setId( 2L );

        List<Answer> practitionerAnswers = new ArrayList<>(
                List.of( new Answer( 1L,
                        null,
                        null,
                        null,
                        mockPractitioner,
                        anotherQuestion ) )
        );

        AnswerDTO answerDTO = new AnswerDTO(
                1,
                "content",
                "true",
                null,
                mockPractitioner.getId(),
                question.getId()
        );

        Answer expectedAnswer = new Answer(
                1L,
                "content",
                "true",
                "Correct",
                mockPractitioner,
                question
                );

        when(questionFacade.getQuestion( question.getId() )).thenReturn( question );
        when( practitionerFacade.getPractitioner( mockPractitioner.getId() )).thenReturn( mockPractitioner );
        when(answerRepository.save(any())).thenReturn( expectedAnswer );

        answerManager.addNewAnswer( answerDTO );

        ArgumentCaptor<Answer> captor = ArgumentCaptor.forClass( Answer.class );
        verify(answerRepository).save( captor.capture() );

        AnswerDTO capturedAnswerDTO = AnswerDTO.mapToDto( captor.getValue() );

        assertEquals( AnswerDTO.mapToDto( expectedAnswer ).getQuestionContent(), capturedAnswerDTO.getQuestionContent() );
        assertEquals( AnswerDTO.mapToDto( expectedAnswer ).getAnswer(), capturedAnswerDTO.getAnswer() );
        assertEquals( AnswerDTO.mapToDto( expectedAnswer ).getResult(), capturedAnswerDTO.getResult() );
        assertEquals( AnswerDTO.mapToDto( expectedAnswer ).getPractitionerId(), capturedAnswerDTO.getPractitionerId() );
        assertEquals( AnswerDTO.mapToDto( expectedAnswer ).getQuestionId(), capturedAnswerDTO.getQuestionId() );
    }

    @Test
    public void testAddNewAnswer_QuestionIsAnswered() {
        Practitioner mockPractitioner = mock(Practitioner.class);
        when(mockPractitioner.getId()).thenReturn( 1L );

        Question mockQuestion = mock(Question.class);
        when(mockQuestion.getId()).thenReturn( 1L );

        List<Answer> practitionerAnswers = new ArrayList<>(
                List.of(new Answer(
                        1L,
                        null,
                        null,
                        null,
                        mockPractitioner,
                        mockQuestion ))
        );

        AnswerDTO answerDTO = new AnswerDTO(
                1,
                "content",
                "YES",
                null,
                mockPractitioner.getId(),
                mockQuestion.getId()
        );

        assertThrows( RuntimeException.class,() -> answerManager.addNewAnswer( answerDTO ) );
    }
}