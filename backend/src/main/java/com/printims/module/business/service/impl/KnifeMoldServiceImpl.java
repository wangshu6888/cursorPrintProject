package com.printims.module.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.printims.common.api.PageResult;
import com.printims.common.exception.BusinessException;
import com.printims.module.business.dto.KnifeLabelVO;
import com.printims.module.business.dto.KnifeMoldQuery;
import com.printims.module.business.entity.KnifeMold;
import com.printims.module.business.mapper.KnifeMoldMapper;
import com.printims.module.business.service.KnifeMoldService;
import org.springframework.transaction.annotation.Transactional;
import com.printims.util.BizNoUtil;
import com.printims.util.KnifeMoldRules;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * 刀模服务实现。
 */
@Service
public class KnifeMoldServiceImpl extends ServiceImpl<KnifeMoldMapper, KnifeMold> implements KnifeMoldService {

    @Override
    public PageResult<KnifeMold> pageQuery(KnifeMoldQuery query) {
        Page<KnifeMold> p = new Page<>(query.getPageNum(), query.getPageSize());
        LambdaQueryWrapper<KnifeMold> w = new LambdaQueryWrapper<>();
        if (StringUtils.hasText(query.getKeyword())) {
            String k = query.getKeyword().trim();
            w.and(x -> x.like(KnifeMold::getMoldName, k)
                    .or().like(KnifeMold::getMoldNo, k)
                    .or().like(KnifeMold::getModel, k)
                    .or().like(KnifeMold::getLocationCode, k));
        }
        if (StringUtils.hasText(query.getStatus())) {
            w.eq(KnifeMold::getStatus, query.getStatus());
        }
        w.orderByDesc(KnifeMold::getCreateTime);
        Page<KnifeMold> result = page(p, w);
        return PageResult.of(result.getRecords(), result.getTotal(), result.getCurrent(), result.getSize());
    }

    @Override
    @CacheEvict(value = "dashboard", allEntries = true)
    public void saveMold(KnifeMold entity) {
        validateShape(entity);

        if (!StringUtils.hasText(entity.getPositionNo())) {
            List<KnifeMold> siblings = lambdaQuery()
                    .eq(KnifeMold::getAreaCode, entity.getAreaCode())
                    .eq(KnifeMold::getShelfNo, entity.getShelfNo())
                    .eq(KnifeMold::getLayerNo, entity.getLayerNo())
                    .apply(entity.getId() != null, "id <> {0}", entity.getId())
                    .list();
            int maxNo = 0;
            for (KnifeMold m : siblings) {
                if (StringUtils.hasText(m.getPositionNo())) {
                    try {
                        int n = Integer.parseInt(m.getPositionNo().trim());
                        if (n > maxNo) maxNo = n;
                    } catch (NumberFormatException ignored) {
                    }
                }
            }
            entity.setPositionNo(String.valueOf(maxNo + 1));
        }

        entity.setLocationCode(KnifeMoldRules.buildLocationCode(
                entity.getAreaCode(), entity.getShelfNo(), entity.getLayerNo(), entity.getPositionNo()));

        if (entity.getId() == null) {
            long count = lambdaQuery()
                    .eq(KnifeMold::getLocationCode, entity.getLocationCode())
                    .count();
            if (count > 0) {
                throw new BusinessException("该位置已被占用");
            }
        } else {
            KnifeMold existing = getById(entity.getId());
            if (existing != null && !entity.getLocationCode().equals(existing.getLocationCode())) {
                long count = lambdaQuery()
                        .eq(KnifeMold::getLocationCode, entity.getLocationCode())
                        .count();
                if (count > 0) {
                    throw new BusinessException("该位置已被占用");
                }
            }
        }

        if (!StringUtils.hasText(entity.getModel())) {
            entity.setModel(KnifeMoldRules.buildModel(
                    entity.getShapeType(), entity.getLength(), entity.getWidth(), entity.getDiameter(),
                    entity.getCustomModelSuffix()));
        }
        if (!StringUtils.hasText(entity.getMoldNo())) {
            entity.setMoldNo(BizNoUtil.moldNo());
        }
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus("IN_STOCK");
        }
        if (entity.getId() == null && StringUtils.hasText(entity.getMoldName())) {
            String shapeCn = switch (entity.getShapeType() != null ? entity.getShapeType().toUpperCase() : "") {
                case "SQUARE" -> "正方形";
                case "CIRCLE" -> "圆形";
                case "CUSTOM" -> "异型";
                default -> "矩形";
            };
            String suffix = "-" + shapeCn + "-" + entity.getModel();
            if (!entity.getMoldName().endsWith(suffix)) {
                entity.setMoldName(entity.getMoldName() + suffix);
            }
        }
        saveOrUpdate(entity);
    }

    private void validateShape(KnifeMold e) {
        String t = e.getShapeType() == null ? "" : e.getShapeType().toUpperCase();
        e.setShapeType(t);
        if ("RECTANGLE".equals(t) || "SQUARE".equals(t)) {
            if (e.getLength() == null || e.getWidth() == null) {
                throw new BusinessException("矩形/正方形刀模需填写长与宽");
            }
        } else if ("CIRCLE".equals(t)) {
            if (e.getDiameter() == null) {
                throw new BusinessException("圆形刀模需填写直径");
            }
        }
    }

    @Override
    public List<KnifeLabelVO> buildLabels(List<Long> ids) {
        List<KnifeLabelVO> list = new ArrayList<>();
        for (Long id : ids) {
            KnifeMold m = getById(id);
            if (m == null) {
                continue;
            }
            KnifeLabelVO vo = new KnifeLabelVO();
            vo.setId(m.getId());
            vo.setMoldNo(m.getMoldNo());
            vo.setShapeType(m.getShapeType());
            vo.setModel(m.getModel());
            vo.setLocationCode(m.getLocationCode());
            vo.setLength(m.getLength());
            vo.setWidth(m.getWidth());
            vo.setDiameter(m.getDiameter());
            vo.setRemark(m.getRemark());
            vo.setQrContent("MOLD:" + m.getId());
            list.add(vo);
        }
        return list;
    }
}
