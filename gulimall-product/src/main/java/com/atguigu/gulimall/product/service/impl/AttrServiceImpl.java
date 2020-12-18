package com.atguigu.gulimall.product.service.impl;

import com.atguigu.common.constant.ProductConstant;
import com.atguigu.common.utils.PageUtils;
import com.atguigu.common.utils.Query;
import com.atguigu.gulimall.product.dao.AttrAttrgroupRelationDao;
import com.atguigu.gulimall.product.dao.AttrDao;
import com.atguigu.gulimall.product.dao.AttrGroupDao;
import com.atguigu.gulimall.product.dao.CategoryDao;
import com.atguigu.gulimall.product.entity.AttrAttrgroupRelationEntity;
import com.atguigu.gulimall.product.entity.AttrEntity;
import com.atguigu.gulimall.product.entity.AttrGroupEntity;
import com.atguigu.gulimall.product.entity.CategoryEntity;
import com.atguigu.gulimall.product.service.AttrAttrgroupRelationService;
import com.atguigu.gulimall.product.service.AttrService;
import com.atguigu.gulimall.product.service.CategoryService;
import com.atguigu.gulimall.product.vo.AttrGroupRelationVo;
import com.atguigu.gulimall.product.vo.AttrResponseVo;
import com.atguigu.gulimall.product.vo.AttrVo;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service("attrService")
public class AttrServiceImpl extends ServiceImpl<AttrDao, AttrEntity> implements AttrService {

    @Autowired
    AttrAttrgroupRelationService attrAttrgroupRelationService;

    @Autowired
    AttrGroupDao attrGroupDao;

    @Autowired
    CategoryDao categoryDao;

    @Autowired
    AttrAttrgroupRelationDao attrAttrgroupRelationDao;

    @Autowired
    CategoryService categoryService;

    @Override
    public PageUtils queryBaseAttrPage(Map<String, Object> params, Long catelogId, String type) {
        QueryWrapper<AttrEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("attr_type","base".equalsIgnoreCase(type)?ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode():ProductConstant.AttrEnum.ATTR_TYPE_SALE.getCode());
        if (catelogId != 0) {
            queryWrapper.eq("catelog_id", catelogId);
        }
        String key = (String) params.get("key");
        if (!StringUtils.isEmpty(key)) {
            //attr_id attr_name
            queryWrapper.and((wrapper) -> {
                wrapper.eq("attr_id", key).or().like("attr_name", key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params),
                queryWrapper);
        PageUtils pageUtils = new PageUtils(page);
        List<AttrEntity> records = page.getRecords();
        List<AttrResponseVo> attrResponseVoList = records.stream()
                .map((attrEntity) -> {
                    AttrResponseVo attrResponseVo = new AttrResponseVo();
                    BeanUtils.copyProperties(attrEntity, attrResponseVo);
                    //设置另外两个属性
                    // 设置属性组名称
                    if("base".equalsIgnoreCase(type)){
                        AttrAttrgroupRelationEntity attr_id = attrAttrgroupRelationDao.
                                selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().
                                        eq("attr_id", attrEntity.getAttrId()));
                        if (attr_id != null && attr_id.getAttrGroupId()!=null) {
                            Long attrGroupId = attr_id.getAttrGroupId();
                            AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrGroupId);
                            attrResponseVo.setGroupName(attrGroupEntity.getAttrGroupName());
                        }
                    }
                    //设置分类属性名称
                    CategoryEntity categoryEntity = categoryDao.selectById(attrEntity.getCatelogId());
                    if (categoryEntity != null) {
                        attrResponseVo.setCatelogName(categoryEntity.getName());
                    }
                    return attrResponseVo;
                }).collect(Collectors.toList());
        pageUtils.setList(attrResponseVoList);
        return pageUtils;
    }

    @Transactional
    @Override
    public void saveAttr(AttrVo attrVo) {
        //attr表
        AttrEntity attrEntity = new AttrEntity();
        BeanUtils.copyProperties(attrVo, attrEntity);
        this.save(attrEntity);
        //attr_attrgroup_relation表
        if(attrVo.getAttrType()== ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode() && attrVo.getAttrGroupId()!=null){//基本属性， 才需要更新属性和属性组关联表， 销售属性不需要
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            relationEntity.setAttrId(attrEntity.getAttrId());
            attrAttrgroupRelationService.save(relationEntity);
        }

    }

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        IPage<AttrEntity> page = this.page(
                new Query<AttrEntity>().getPage(params),
                new QueryWrapper<AttrEntity>()
        );

        return new PageUtils(page);
    }

    @Override
    public AttrResponseVo getAttrInfo(Long attrId) {
        AttrResponseVo respVo = new AttrResponseVo();
        AttrEntity attrEntity = this.getById(attrId);
        BeanUtils.copyProperties(attrEntity, respVo);

        if(attrEntity.getAttrType()==ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){//基本属性才需要查询分组信息，销售属性不需要
            //设置分组信息
            AttrAttrgroupRelationEntity attrgroupRelationEntity = attrAttrgroupRelationDao.selectOne(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrId));
            if (attrgroupRelationEntity != null) {
                respVo.setAttrGroupId(attrgroupRelationEntity.getAttrGroupId());
                AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrId);
                if (attrGroupEntity != null) {
                    respVo.setGroupName(attrGroupEntity.getAttrGroupName());
                }
            }
        }


        //设置分类信息
        Long catelogId = attrEntity.getCatelogId();
        Long[] catelogPath = categoryService.findCatelogPath(catelogId);

        //设置分类信息
        CategoryEntity categoryEntity = categoryDao.selectById(catelogId);
        if (categoryEntity != null) {
            respVo.setCatelogPath(catelogPath);
            respVo.setCatelogName(categoryEntity.getName());
        }
        return respVo;
    }

    @Transactional
    @Override
    public void updateAttr(AttrVo attrVo) {
        AttrEntity attrEntity=new AttrEntity();
        BeanUtils.copyProperties(attrVo,attrEntity);
        //修改属性表
        this.updateById(attrEntity);
        if(attrEntity.getAttrType()==ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode()){//基本属性才需要同时修改关联表，销售属性不要
            //修改分组关联表
            AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
            relationEntity.setAttrId(attrVo.getAttrId());
            relationEntity.setAttrGroupId(attrVo.getAttrGroupId());
            //查询当前属性有没有分组关联属性
            Integer count = attrAttrgroupRelationDao.selectCount(new QueryWrapper<AttrAttrgroupRelationEntity>().eq("attr_id", attrVo.getAttrId()));
            if(count>0){
                attrAttrgroupRelationDao.update(relationEntity,new UpdateWrapper<AttrAttrgroupRelationEntity>().eq("attr_id",attrVo.getAttrId()));
            }else{
                attrAttrgroupRelationDao.insert(relationEntity);
            }
        }
    }

    //根据属性组id 查询所有关联的基本属性
    @Override
    public List<AttrEntity> getRelationAttr(Long attrgroupId) {
        QueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("attr_group_id",attrgroupId);
        List<AttrAttrgroupRelationEntity> attrgroupRelationEntities = attrAttrgroupRelationDao.selectList(queryWrapper);
        List<Long> attrIds = attrgroupRelationEntities.stream()
                .map((item) -> {
                    return item.getAttrId();
                }).collect(Collectors.toList());
        if(attrIds==null || attrIds.size()==0){
            return null;
        }
        List<AttrEntity> attrEntities = this.listByIds(attrIds);
        return attrEntities;
    }

    @Override
    public void deleteRelation(AttrGroupRelationVo[] vos) {
        QueryWrapper<AttrAttrgroupRelationEntity> queryWrapper = new QueryWrapper<>();
        //queryWrapper.eq("attr_id",1L).eq("attr_group_id",1L);
        List<AttrAttrgroupRelationEntity> relationEntities = Arrays.asList(vos)
                .stream()
                .map((item) -> {
                    AttrAttrgroupRelationEntity relationEntity = new AttrAttrgroupRelationEntity();
                    BeanUtils.copyProperties(item, relationEntity);
                    return relationEntity;
                }).collect(Collectors.toList());
        attrAttrgroupRelationDao.deleteBatchRelation(relationEntities);
    }

    /**
     * 获取没有与当前属性组关联的所有属性
     * @param params  分页参数
     * @param attrgroupId 属性组id
     * @return
     */
    @Override
    public PageUtils getNoRelationAttr(Map<String, Object> params, Long attrgroupId) {
        //1， 当前分组只能关联自己所属的分类里面的所有属性
        AttrGroupEntity attrGroupEntity = attrGroupDao.selectById(attrgroupId);
        Long catelogId = attrGroupEntity.getCatelogId();
        //2，当前分组只能关联其他分组没有引用的属性， 即一个属性只能绑定一个分组
            //2.1 找到当前分类下的其他分组
        QueryWrapper<AttrGroupEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("catelog_id",catelogId);//.ne("attr_group_id",attrgroupId)
        List<AttrGroupEntity> group = attrGroupDao.selectList(queryWrapper);
        //2.2 找到这些其他分组关联的属性
            //提取这些分组的id
        List<Long> groupIds = group.stream()
                .map((item) -> {
                    return item.getAttrGroupId();
                }).collect(Collectors.toList());
        QueryWrapper<AttrAttrgroupRelationEntity> queryWrapper1 = new QueryWrapper<>();
        queryWrapper1.in("attr_group_id", groupIds);
        List<AttrAttrgroupRelationEntity> otherAttrs = attrAttrgroupRelationDao.selectList(queryWrapper1);
            //提取这些其他属性的id
        List<Long> otherAttrIds = otherAttrs.stream()
                .map((item) -> {
                    return item.getAttrId();
                }).collect(Collectors.toList());
        //otherAttrIds 包含其他分组里面的所有属性id

            //2.3 从当前分类的所有属性中排除这些属性，得到没有关联的属性
        QueryWrapper<AttrEntity> queryWrapper2 = new QueryWrapper<>();
        queryWrapper2.eq("catelog_id",catelogId).eq("attr_type",ProductConstant.AttrEnum.ATTR_TYPE_BASE.getCode());
        if(otherAttrIds!=null && otherAttrIds.size()>0){
            queryWrapper2.notIn("attr_id",otherAttrIds);
        }
        //分页返回
        String key =(String) params.get("key");//模糊查询
        if(!StringUtils.isEmpty(key)){
            queryWrapper2.and((w)->{
               w.eq("attr_id",key).or().like("attr_name",key);
            });
        }
        IPage<AttrEntity> page = this.page(new Query<AttrEntity>().getPage(params), queryWrapper2);

        PageUtils pageUtils = new PageUtils(page);
        return pageUtils;
    }

    @Override
    public List<Long> selectSearchAttrsIds(List<Long> attrIds) {
        //SELECT attr_id FROM `pms_attr` WHERE attr_id IN(?) AND search_type=1
       return baseMapper.selectSearchAttrsIds(attrIds);
    }
}