package ru.practicum.shareit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.json.JsonTest;
import org.springframework.boot.test.json.JacksonTester;
import ru.practicum.shareit.request.ItemRequestDto;

import static org.assertj.core.api.Assertions.assertThat;

@JsonTest
class ItemRequestDtoTest {

    @Autowired
    private JacksonTester<ItemRequestDto> json;

    @Test
    void serialize_shouldWriteDescriptionField() throws Exception {
        // given
        ItemRequestDto dto = new ItemRequestDto();
        dto.setDescription("Нужна дрель для ремонта");

        // when
        var result = json.write(dto);

        // then
        assertThat(result).hasJsonPath("$.description");
        assertThat(result).extractingJsonPathStringValue("$.description")
                .isEqualTo("Нужна дрель для ремонта");
    }

    @Test
    void deserialize_shouldReadDescriptionField() throws Exception {
        // given
        String content = "{\"description\":\"Нужна дрель\"}";

        // when
        ItemRequestDto result = json.parse(content).getObject();

        // then
        assertThat(result.getDescription()).isEqualTo("Нужна дрель");
    }

    @Test
    void deserialize_emptyDescription_shouldNotFail() throws Exception {
        // given
        String content = "{\"description\":\"\"}";

        // when
        ItemRequestDto result = json.parse(content).getObject();

        // then
        assertThat(result.getDescription()).isEmpty();
    }
}