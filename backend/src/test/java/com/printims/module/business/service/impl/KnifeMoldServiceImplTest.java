package com.printims.module.business.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.printims.common.api.PageResult;
import com.printims.common.exception.BusinessException;
import com.printims.module.business.dto.KnifeLabelVO;
import com.printims.module.business.dto.KnifeMoldQuery;
import com.printims.module.business.entity.KnifeMold;
import com.printims.module.business.mapper.KnifeMoldMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class KnifeMoldServiceImplTest {

    @Mock
    private KnifeMoldMapper knifeMoldMapper;

    private KnifeMoldServiceImpl knifeMoldService;

    @BeforeEach
    void setUp() {
        knifeMoldService = new KnifeMoldServiceImpl() {
            @Override
            public KnifeMoldMapper getBaseMapper() {
                return knifeMoldMapper;
            }
        };
    }

    @Test
    void pageQuery() {
        KnifeMoldQuery query = new KnifeMoldQuery();
        query.setPageNum(1);
        query.setPageSize(10);
        query.setKeyword("test");

        Page<KnifeMold> mockPage = new Page<>();
        mockPage.setRecords(List.of(new KnifeMold()));
        mockPage.setTotal(1);

        when(knifeMoldMapper.selectPage(any(), any())).thenReturn(mockPage);

        PageResult<KnifeMold> result = knifeMoldService.pageQuery(query);
        assertNotNull(result);
        assertEquals(1, result.getTotal());
        assertEquals(1, result.getRecords().size());
    }

    @Test
    void saveMold_Rectangle_Success() {
        KnifeMold mold = new KnifeMold();
        mold.setShapeType("RECTANGLE");
        mold.setLength(new BigDecimal("10"));
        mold.setWidth(new BigDecimal("5"));
        mold.setAreaCode("A");
        mold.setShelfNo("1");
        mold.setLayerNo("1");
        mold.setPositionNo("1");

        KnifeMoldServiceImpl serviceSpy = spy(knifeMoldService);
        doReturn(true).when(serviceSpy).saveOrUpdate(any(KnifeMold.class));

        serviceSpy.saveMold(mold);

        assertEquals("A-01-01-01", mold.getLocationCode());
        assertEquals("10×5", mold.getModel());
        assertNotNull(mold.getMoldNo());
        assertEquals("IN_STOCK", mold.getStatus());
        verify(serviceSpy).saveOrUpdate(any(KnifeMold.class));
    }

    @Test
    void saveMold_Rectangle_MissingDimensions() {
        KnifeMold mold = new KnifeMold();
        mold.setShapeType("RECTANGLE");

        BusinessException ex = assertThrows(BusinessException.class, () -> knifeMoldService.saveMold(mold));
        assertEquals("矩形/正方形刀模需填写长与宽", ex.getMessage());
    }

    @Test
    void saveMold_Circle_Success() {
        KnifeMold mold = new KnifeMold();
        mold.setShapeType("CIRCLE");
        mold.setDiameter(new BigDecimal("15"));
        mold.setAreaCode("B");
        mold.setShelfNo("2");
        mold.setLayerNo("2");
        mold.setPositionNo("2");

        KnifeMoldServiceImpl serviceSpy = spy(knifeMoldService);
        doReturn(true).when(serviceSpy).saveOrUpdate(any(KnifeMold.class));

        serviceSpy.saveMold(mold);

        assertEquals("B-02-02-02", mold.getLocationCode());
        assertEquals("D15", mold.getModel());
        assertNotNull(mold.getMoldNo());
        assertEquals("IN_STOCK", mold.getStatus());
        verify(serviceSpy).saveOrUpdate(any(KnifeMold.class));
    }

    @Test
    void saveMold_Circle_MissingDiameter() {
        KnifeMold mold = new KnifeMold();
        mold.setShapeType("CIRCLE");

        BusinessException ex = assertThrows(BusinessException.class, () -> knifeMoldService.saveMold(mold));
        assertEquals("圆形刀模需填写直径", ex.getMessage());
    }

    @Test
    void buildLabels() {
        KnifeMold mold1 = new KnifeMold();
        mold1.setId(1L);
        mold1.setMoldNo("M1");
        mold1.setModel("Model-1");
        mold1.setLocationCode("Loc-1");

        KnifeMold mold2 = new KnifeMold();
        mold2.setId(2L);
        mold2.setMoldNo("M2");
        mold2.setModel("Model-2");
        mold2.setLocationCode("Loc-2");

        KnifeMoldServiceImpl serviceSpy = spy(knifeMoldService);
        doReturn(mold1).when(serviceSpy).getById(1L);
        doReturn(mold2).when(serviceSpy).getById(2L);

        List<KnifeLabelVO> labels = serviceSpy.buildLabels(Arrays.asList(1L, 2L, 3L));

        assertEquals(2, labels.size());
        assertEquals("M1", labels.get(0).getMoldNo());
        assertEquals("MOLD:1", labels.get(0).getQrContent());
        assertEquals("M2", labels.get(1).getMoldNo());
    }
}
