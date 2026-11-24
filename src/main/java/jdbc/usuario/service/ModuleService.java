
package jdbc.usuario.service;

import java.util.List;
import jdbc.usuario.persistence.entity.ModuleEntity;

public interface ModuleService {

    List<ModuleEntity> findAll();
}
