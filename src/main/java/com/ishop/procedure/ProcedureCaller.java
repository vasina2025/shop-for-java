package com.ishop.procedure;

import com.ishop.yaml.ProcedureConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProcedureCaller {

    private final JdbcTemplate jdbcTemplate;
    private final ProcedureConfig procedureConfig;

    public List<Map<String, Object>> callProcedure(String procedureName, Object... params) {
        try {
            ProcedureConfig.ProcedureDefinition procedure = procedureConfig.getProcedures().get(procedureName);
            if (procedure == null) {
                log.warn("Procedure not found: {}", procedureName);
                return new ArrayList<>();
            }

            String sql = procedure.getSql();
            log.info("Executing: {} with params: {}", procedureName, params);

            if (sql.trim().toUpperCase().startsWith("SELECT")) {
                return jdbcTemplate.queryForList(sql, params);
            } else {
                jdbcTemplate.update(sql, params);
                return new ArrayList<>();
            }
        } catch (Exception e) {
            log.error("Error calling {}: {}", procedureName, e.getMessage());
            return new ArrayList<>();
        }
    }

    public Map<String, Object> callProcedureSingle(String procedureName, Object... params) {
        List<Map<String, Object>> results = callProcedure(procedureName, params);
        if (results != null && !results.isEmpty()) {
            return results.get(0);
        }
        return null;
    }
}
