package com.example.demo.repository;

import com.example.demo.dto.ChildDTO;
import com.example.demo.dto.GroupDTO;
import com.example.demo.entity.QGroup;
import com.example.demo.users.entity.QChild;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Repository
public class GroupQueryRepositoryImpl implements GroupQueryRepository{

    private final JPAQueryFactory queryFactory;

    private final QGroup g = QGroup.group;
    private final QChild c = QChild.child;

    // groupId로 그룹과 자녀 리스트를 한 번에 조회
    @Override
    public Optional<GroupDTO> getGroupWithChildrenByGroupId(Long groupId) {

        List<Tuple> tuples = queryFactory
                .select(
                        g.id, g.groupName, g.groupEmail, g.groupPhoneNumber, g.targetGroup.stringValue(),
                        c.id, c.birthOrder, c.name, c.nickname, c.weight, c.height,
                        c.gender, c.riskGroup, c.birthday
                )
                .from(g)
                .leftJoin(g.children, c)   // ★ 여기!
                .where(g.id.eq(groupId))
                .fetch();

        GroupDTO groupDTO = null;
        List<ChildDTO> childList = new ArrayList<>();

        for (Tuple t : tuples) {
            if (groupDTO == null) {
                groupDTO = GroupDTO.builder()
                        .id(t.get(g.id))
                        .groupName(t.get(g.groupName))
                        .groupEmail(t.get(g.groupEmail))
                        .targetGroup(t.get(g.targetGroup.stringValue()))
                        .groupPhoneNumber(t.get(g.groupPhoneNumber))
                        .children(childList)
                        .build();
            }
            Long childId = t.get(c.id);
            if (childId != null) {
                ChildDTO child = ChildDTO.builder()
                        .id(childId)
                        .birthOrder(t.get(c.birthOrder))
                        .name(t.get(c.name))
                        .nickname(t.get(c.nickname))
                        .weight(t.get(c.weight))
                        .height(t.get(c.height))
                        .gender(t.get(c.gender))
                        .riskGroup(t.get(c.riskGroup))
                        .birthday(t.get(c.birthday))
                        .build();
                childList.add(child);
            }
        }

        return Optional.ofNullable(groupDTO);
    }

}
