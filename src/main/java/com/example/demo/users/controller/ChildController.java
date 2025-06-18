// 경로: com.example.demo.users.controller.ChildController

package com.example.demo.users.controller;

import com.example.demo.users.entity.Child;
import com.example.demo.users.service.ChildService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequiredArgsConstructor
public class ChildController {

    private final ChildService childService;

    /**
     * 자녀 ID를 기반으로 자녀의 상세 정보를 반환하는 API입니다.
     *
     * <p>프론트엔드에서 자녀를 선택했을 때, 선택된 자녀의 이름, 성별, 나이, 키, 체중 정보를
     * 비동기(AJAX)로 받아와서 화면에 동적으로 표시할 때 사용됩니다.</p>
     *
     * @param childId 조회할 자녀의 ID
     * @return 자녀의 상세 정보(name, gender, age, height, weight)를 담은 Map
     * 예: {
     * "name": "홍길동",
     * "gender": "남",
     * "age": 3,
     * "height": 95.0,
     * "weight": 15.3
     * }
     */
    @GetMapping("/api/child/info/{childId}")
    public Map<String, Object> getChildInfo(@PathVariable Long childId) {
        Child child = childService.findById(childId);
        Map<String, Object> result = new HashMap<>();
        result.put("name", child.getName());
        result.put("gender", child.getGender());
        result.put("age", child.getAge());
        result.put("height", child.getHeight());
        result.put("weight", child.getWeight());
        return result;
    }
}

