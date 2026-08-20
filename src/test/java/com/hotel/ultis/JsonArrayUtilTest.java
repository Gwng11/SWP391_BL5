package com.hotel.ultis;

import java.util.List;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class JsonArrayUtilTest {
    @Test void addUpdateMoveAndRemoveKeepValidJson(){String json=JsonArrayUtil.addUnique("[]","WiFi");json=JsonArrayUtil.addUnique(json,"TV");json=JsonArrayUtil.replace(json,1,"Smart TV");json=JsonArrayUtil.move(json,1,-1);assertEquals(List.of("Smart TV","WiFi"),JsonArrayUtil.parse(json));assertEquals("[\"WiFi\"]",JsonArrayUtil.remove(json,0));}
    @Test void duplicateAndBadIndexAreRejected(){assertThrows(IllegalArgumentException.class,()->JsonArrayUtil.addUnique("[\"WiFi\"]","WiFi"));assertThrows(IllegalArgumentException.class,()->JsonArrayUtil.remove("[]",0));}
}
