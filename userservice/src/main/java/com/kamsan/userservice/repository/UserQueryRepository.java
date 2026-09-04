package com.kamsan.userservice.repository;

import com.kamsan.userservice.dto.DeviceDTO;
import com.kamsan.userservice.dto.PageUserDTO;
import com.kamsan.userservice.dto.TicketUserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Service;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static com.kamsan.userservice.repository.query.UserQuery.*;

@Service
@RequiredArgsConstructor
public class UserQueryRepository {

    private final JdbcClient jdbc;

    public List<PageUserDTO> getUsersPage(Pageable page) {
        return jdbc.sql(FIND_USERS_PAGE_QUERY)
                   .param("limit", page.getPageSize())
                   .param("offset", page.getOffset())
                   .query((rs, rowNum) -> new PageUserDTO(
                           rs.getObject("user_public_id", UUID.class),
                           rs.getString("email"),
                           rs.getString("first_name"),
                           rs.getString("last_name"),
                           rs.getString("member_id"),
                           rs.getString("bio"),
                           rs.getString("image_url"),
                           rs.getString("phone"),
                           rs.getString("address"),
                           rs.getObject("created_at", OffsetDateTime.class),
                           rs.getString("role"),
                           rs.getString("authorities")
                   ))
                   .list();
    }

    public Long countTotalUsers() {
        return jdbc.sql(COUNT_TOTAL_USERS_QUERY)
                   .query(Long.class)
                   .single();
    }

    public TicketUserDTO getAssignee(UUID ticketPublicId) {
        return jdbc.sql(SELECT_TICKET_ASSIGNEE_QUERY)
                   .param("ticketPublicId", ticketPublicId)
                   .query((rs, rowNum) -> new TicketUserDTO(
                           rs.getObject("user_public_id", UUID.class),
                           rs.getString("email"),
                           rs.getString("first_name"),
                           rs.getString("last_name"),
                           rs.getString("image_url")
                   ))
                   .single();
    }

    public List<DeviceDTO> getUserDevices(UUID userPublicId) {
        return jdbc.sql(SELECT_DEVICES_QUERY)
                   .param("userPublicId", userPublicId)
                   .query((rs, rowNum) -> new DeviceDTO(
                           rs.getString("machine"),
                           rs.getString("client"),
                           rs.getString("ip_address")
                   ))
                   .list();
    }
}
