
package jdbc.usuario.service;

import java.util.List;
import org.springframework.stereotype.Service;
import jdbc.usuario.persistence.ModuleDAO;
import jdbc.usuario.persistence.entity.ModuleEntity;

@Service
public class ModuleServiceImpl implements ModuleService {

    private final ModuleDAO moduleDAO;

    public ModuleServiceImpl(ModuleDAO moduleDAO) {
        this.moduleDAO = moduleDAO;
    }

    @Override
    public List<ModuleEntity> findAll() {
        return moduleDAO.findAll();
    }
}
