package iuh.house_keeping_service_be.services.AdminService.impl;

import iuh.house_keeping_service_be.dtos.Admin.UserPermission.response.*;
import iuh.house_keeping_service_be.enums.RoleName;
import iuh.house_keeping_service_be.models.*;
import iuh.house_keeping_service_be.repositories.*;
import iuh.house_keeping_service_be.services.AdminService.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PermissionServiceImpl implements PermissionService {

    private final RoleRepository roleRepository;
    private final FeatureRepository featureRepository;
    private final RoleFeatureRepository roleFeatureRepository;
    private final AccountRepository accountRepository;
    private final AccountRoleRepository accountRoleRepository;

    @Override
    public RoleListResponse getAllManageableRoles() {
        try {
            List<Role> roles = roleRepository.findNonAdminRoles();

            List<RoleData> roleDataList = roles.stream()
                .map(role -> new RoleData(role.getRoleId(), role.getRoleName().name()))
                .collect(Collectors.toList());

            return new RoleListResponse(true, "Lấy danh sách vai trò thành công", roleDataList);

        } catch (Exception e) {
            log.error("Error getting manageable roles: {}", e.getMessage());
            return new RoleListResponse(false, "Lỗi khi lấy danh sách vai trò", Collections.emptyList());
        }
    }

    @Override
    public PermissionManagementResponse getRolePermissions(Integer roleId) {
        try {
            Optional<Role> roleOpt = roleRepository.findById(roleId);
            if (roleOpt.isEmpty()) {
                return new PermissionManagementResponse(false, "Vai trò không tồn tại", Collections.emptyList());
            }

            Role role = roleOpt.get();

            // Lấy tất cả features
            List<Feature> allFeatures = featureRepository.findAllOrderByModuleAndName();

            // Lấy các quyền hiện tại của vai trò
            List<RoleFeature> roleFeatures = roleFeatureRepository.findByRoleId(roleId);
            Map<Integer, Boolean> featureStatusMap = roleFeatures.stream()
                .collect(Collectors.toMap(
                    rf -> rf.getFeature().getFeatureId(),
                    RoleFeature::getIsEnabled
                ));

            // Nhóm features theo module
            Map<String, List<Feature>> featuresByModule = allFeatures.stream()
                .collect(Collectors.groupingBy(Feature::getModule));

            int roleAdminId = roleRepository.findByRoleName(RoleName.ADMIN)
                .map(Role::getRoleId)
                .orElse(-1);

            List<ModulePermissionData> modules;

            if (!roleId.equals(roleAdminId)) {
                modules = featuresByModule.entrySet().stream()
                        .map(entry -> {
                            String moduleName = entry.getKey();
                            List<FeaturePermissionData> features = entry.getValue().stream()
                                    .map(feature -> new FeaturePermissionData(
                                            feature.getFeatureId(),
                                            feature.getFeatureName(),
                                            feature.getDescription(),
                                            featureStatusMap.getOrDefault(feature.getFeatureId(), false)
                                    ))
                                    .collect(Collectors.toList());

                            return new ModulePermissionData(moduleName, features);
                        })
                        .filter(module -> !"ADMIN".equalsIgnoreCase(module.moduleName()))
                        .sorted(Comparator.comparing(ModulePermissionData::moduleName))
                        .collect(Collectors.toList());
            } else {
                modules = featuresByModule.entrySet().stream()
                        .map(entry -> {
                            String moduleName = entry.getKey();
                            List<FeaturePermissionData> features = entry.getValue().stream()
                                    .map(feature -> new FeaturePermissionData(
                                            feature.getFeatureId(),
                                            feature.getFeatureName(),
                                            feature.getDescription(),
                                            featureStatusMap.getOrDefault(feature.getFeatureId(), false)
                                    ))
                                    .collect(Collectors.toList());

                            return new ModulePermissionData(moduleName, features);
                        })
                        .sorted(Comparator.comparing(ModulePermissionData::moduleName))
                        .collect(Collectors.toList());
            }

            RolePermissionData rolePermissionData = new RolePermissionData(
                role.getRoleId(),
                role.getRoleName().name(),
                modules
            );

            return new PermissionManagementResponse(
                true,
                "Lấy quyền vai trò thành công",
                Collections.singletonList(rolePermissionData)
            );

        } catch (Exception e) {
            log.error("Error getting role permissions for roleId {}: {}", roleId, e.getMessage());
            return new PermissionManagementResponse(false, "Lỗi khi lấy quyền vai trò", Collections.emptyList());
        }
    }

    @Override
    @Transactional
    public PermissionManagementResponse updateRolePermission(Integer roleId, Integer featureId, Boolean isEnabled) {
        try {
            // Kiểm tra vai trò có tồn tại
            Optional<Role> roleOpt = roleRepository.findById(roleId);
            if (roleOpt.isEmpty()) {
                return new PermissionManagementResponse(false, "Vai trò không tồn tại", Collections.emptyList());
            }

            // Kiểm tra feature có tồn tại
            Optional<Feature> featureOpt = featureRepository.findById(featureId);
            if (featureOpt.isEmpty()) {
                return new PermissionManagementResponse(false, "Chức năng không tồn tại", Collections.emptyList());
            }

            Role role = roleOpt.get();
            Feature feature = featureOpt.get();

            // Tìm hoặc tạo RoleFeature
            Optional<RoleFeature> roleFeatureOpt = roleFeatureRepository.findByRoleIdAndFeatureId(roleId, featureId);

            if (roleFeatureOpt.isPresent()) {
                // Cập nhật trạng thái hiện có
                RoleFeature roleFeature = roleFeatureOpt.get();
                roleFeature.setIsEnabled(isEnabled);
                roleFeatureRepository.save(roleFeature);
            } else {
                // Tạo mới RoleFeature
                RoleFeatureId roleFeatureId = new RoleFeatureId(roleId, featureId);
                RoleFeature newRoleFeature = new RoleFeature();
                newRoleFeature.setId(roleFeatureId);
                newRoleFeature.setRole(role);
                newRoleFeature.setFeature(feature);
                newRoleFeature.setIsEnabled(isEnabled);
                roleFeatureRepository.save(newRoleFeature);
            }

            log.info("Updated permission for role {} feature {}: {}",
                role.getRoleName(), feature.getFeatureName(), isEnabled);

            // Trả về dữ liệu cập nhật
            return getRolePermissions(roleId);

        } catch (Exception e) {
            log.error("Error updating role permission: {}", e.getMessage());
            return new PermissionManagementResponse(false, "Lỗi khi cập nhật quyền", Collections.emptyList());
        }
    }

    @Override
    public UserPermissionsResponse getUserPermissions(String username) {
        try {
            // Tìm account
            List<Account> accounts = accountRepository.findAccountsByUsername(username);
            if (accounts.isEmpty()) {
                return new UserPermissionsResponse(false, "Tài khoản không tồn tại", null);
            }

            Account account = accounts.get(0);

            // Lấy tất cả vai trò của user
            List<AccountRole> accountRoles = accountRoleRepository.findByAccountId(account.getAccountId());

            Set<String> allPermissions = new HashSet<>();
            String primaryRole = null;

            for (AccountRole accountRole : accountRoles) {
                Role role = accountRole.getRole();
                if (primaryRole == null) {
                    primaryRole = role.getRoleName().name();
                }

                // Lấy các quyền được kích hoạt của vai trò này
                List<String> rolePermissions = roleFeatureRepository.findEnabledFeatureNamesByRoleId(role.getRoleId());
                allPermissions.addAll(rolePermissions);
            }

            UserPermissionData data = new UserPermissionData(
                username,
                primaryRole,
                new ArrayList<>(allPermissions)
            );

            return new UserPermissionsResponse(true, "Lấy quyền người dùng thành công", data);

        } catch (Exception e) {
            log.error("Error getting user permissions for {}: {}", username, e.getMessage());
            return new UserPermissionsResponse(false, "Lỗi khi lấy quyền người dùng", null);
        }
    }

    @Override
    public List<String> getEnabledPermissionsByRole(RoleName roleName) {
        try {
            Optional<Role> roleOpt = roleRepository.findByRoleName(roleName);
            if (roleOpt.isEmpty()) {
                return Collections.emptyList();
            }

            return roleFeatureRepository.findEnabledFeatureDescriptionsByRoleId(roleOpt.get().getRoleId());

        } catch (Exception e) {
            log.error("Error getting enabled permissions for role {}: {}", roleName, e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public boolean hasPermission(String username, String featureName) {
        try {
            List<Account> accounts = accountRepository.findAccountsByUsername(username);
            if (accounts.isEmpty()) {
                return false;
            }

            Account account = accounts.get(0);
            List<AccountRole> accountRoles = accountRoleRepository.findByAccountId(account.getAccountId());

            for (AccountRole accountRole : accountRoles) {
                List<String> permissions = roleFeatureRepository.findEnabledFeatureNamesByRoleId(
                    accountRole.getRole().getRoleId()
                );
                if (permissions.contains(featureName)) {
                    return true;
                }
            }

            return false;

        } catch (Exception e) {
            log.error("Error checking permission {} for user {}: {}", featureName, username, e.getMessage());
            return false;
        }
    }
}