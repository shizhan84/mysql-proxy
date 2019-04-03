//package cn.okcoming.dbproxy.client.impl;
//
//import cn.okcoming.dbproxy.bean.*;
//import cn.okcoming.dbproxy.client.JdbcOperationClient;
//import cn.okcoming.dbproxy.core.DBRow;
//import cn.okcoming.dbproxy.core.DBRowMapper;
//import cn.okcoming.dbproxy.core.DefaultDBRow;
//import lombok.extern.slf4j.Slf4j;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//import org.springframework.web.client.RestTemplate;
//import rx.functions.Func1;
//
//
//import java.text.SimpleDateFormat;
//import java.time.LocalDateTime;
//import java.time.format.DateTimeFormatter;
//import java.util.*;
//import java.util.function.Supplier;
//
///**
// * Created by bluces on 2016/12/28.
// */
//@Slf4j
//public class JdbcOperationClientImpl implements JdbcOperationClient {
//    private static final Logger LOG = LoggerFactory.getLogger(JdbcOperationClientImpl.class);
//
//
//    private RestTemplate restTemplate;
//    private String _project;
//    private String _database;
//
//    public void setProject(String project) {
//        this._project = project;
//    }
//
//    public void setDatabase(String database) {
//        this._database = database;
//    }
//
//    private  <T> T execute(String apiName,String member,Supplier<T> supplier){
//        log.info("http {} api: {}",apiName,member);
//        try{
//            T ret = supplier.get();
//            log.debug("{}", ret);
//            return ret;
//        }catch (Exception e){
//            log.error("",e);
//        }
//        return null;
//    }
//
//    /**分页接口数据全都取下来*/
//    private  <RESP extends AbstractPageResponse<DATA>,DATA> List<DATA> fetchAllPage(String url,Class<RESP> classes,Object... uriVariables){
//        final List<DATA> all = new ArrayList<>();
//        RESP response;
//        int pageIndex = 1;
//        do{
//            List variables = Lists.newArrayList(properties.getThirdAppkey());
//            variables.addAll(Arrays.asList(uriVariables));
//            variables.add(pageIndex++);
//            response = restTemplate.getForObject(url,classes, variables.toArray());
//            //response = restTemplate.getForObject(url,classes,properties.getThirdAppkey(),uriVariables,pageIndex++);
//            log.debug("{}", response);
//            if(response.success()) {
//                all.addAll(response.getResult());
//            }else if(response.notfound()){
//                break;
//            }else{
//                log.error("{}", response);
//                return null;
//            }
//        }while(response.getPaging().getPageSize()* response.getPaging().getPageIndex()< response.getPaging().getTotalRecords());
//        return all;
//    }
//
//    /**取一个对象记录*/
//    private  <RESP extends AbstractBaseResponse<DATA>,DATA> DATA fetchOne(String url, Class<RESP> classes, Object... uriVariables){
//        List variables = Lists.newArrayList(properties.getThirdAppkey());
//        variables.addAll(Arrays.asList(uriVariables));
//        RESP response = restTemplate.getForObject(url,classes, variables.toArray());
//        if(response.success()){
//            return response.getResult();
//        }else {
//            log.error("{}",response);
//            return null;
//        }
//    }
//
//    /**取一列对象记录*/
//    private  <RESP extends AbstractListResponse<DATA>,DATA> List<DATA> fetchList(String url, Class<RESP> classes, Object... uriVariables){
//        List variables = Lists.newArrayList(properties.getThirdAppkey());
//        variables.addAll(Arrays.asList(uriVariables));
//        RESP response = restTemplate.getForObject(url,classes, variables.toArray());
//        if(response.success()){
//            return response.getResult();
//        }else if(response.notfound()){
//            return Collections.EMPTY_LIST;
//        }else {
//            log.error("{}",response);
//            return null;
//        }
//    }
//
//
//    private DateTimeFormatter DTF =   DateTimeFormatter.ofPattern("yyyyMMddHHmmss");
//
//    private Object[] convert(Object[] args){
//        if(args == null){
//            return null;
//        }
//        for(int i=0;i<args.length;i++){
//            if(args[i] instanceof Date){
//                args[i] =  new SimpleDateFormat("yyyyMMddHHmmss").format(args[i]);
//            }
//        }
//        return args;
//    }
//
//
//    public void execute(String sql)  {
//
//    }
//
//
//    @Override
//    public <T> Observable<List<T>> queryForList(final String sql, final Class<T> requiredType,Object... args)  {
//        QueryRequest request = new QueryRequest();
//        request.setParameters(convert(args));
//        request.setQuery(sql);
//        request.setProject(_project);
//        request.setDatabase(_database);
//
//        restTemplate.exchange()
//        return _signalClient.interaction().request(request).<QueryResponse>build().map(new Func1<QueryResponse, List<T>>() {
//            @Override
//            public List<T> call(final QueryResponse resp) {
//                LOG.debug("query {}",resp);
//                if (Objects.equals("0", resp.getCode())) {
//                    List<List<Object>> rows = resp.getRowList();
//                    List<String> cols = resp.getColList();
//                    List<Integer> types = resp.getTypeList();
//
//                    List<T> ret = new ArrayList<>();
//                    for (List<Object> row : rows) {//行
//                        T bean = new DefaultDBRow(row, cols, types).getOnlyObject(requiredType);
//                        ret.add(bean);
//                    }
//                    return ret;
//                } else {
//                    LOG.error(resp.getMessage());
//                    throw new RuntimeException(resp.getMessage());
//                }
//            }
//        });
//    }
//
//
//    @Override
//    public <T> Observable<List<T>> query(final String sql, final DBRowMapper<T> rowMapper)  {
//        return this.query(sql,rowMapper,null);
//    }
//
//    @Override
//    public <T> Observable<List<T>> query(final String sql, final DBRowMapper<T> rowMapper,Object... args)  {
//        QueryRequest request = new QueryRequest();
//        request.setParameters(convert(args));
//        request.setQuery(sql);
//        request.setProject(_project);
//        request.setDatabase(_database);
//        return _signalClient.interaction().request(request).<QueryResponse>build().map(new Func1<QueryResponse, List<T>>() {
//
//            @Override
//            public List<T> call(final QueryResponse resp) {
//                if (Objects.equals("0", resp.getCode())) {
//                    List<List<Object>> rows = resp.getRowList();
//                    List<String> cols = resp.getColList();
//                    List<Integer> types = resp.getTypeList();
//
//                    List<T> ret = new ArrayList<>();
//                    for (List<Object> row : rows) {//行
//                        T bean = rowMapper.mapRow(new DefaultDBRow(row, cols, types));
//                        ret.add(bean);
//                    }
//                    return ret;
//                } else {
//                    LOG.error(resp.getMessage());
//                    throw new RuntimeException(resp.getMessage());
//                }
//            }
//        });
//    }
//
//    @Override
//    public <T> Observable<T> queryForObject(final String sql,final DBRowMapper<T> rowMapper, Object... args)  {
//        QueryRequest request = new QueryRequest();
//        request.setParameters(convert(args));
//        request.setQuery(sql);
//        request.setProject(_project);
//        request.setDatabase(_database);
//        LOG.debug("query {}",request);
//        return _signalClient.interaction().request(request).<QueryResponse>build().map(new Func1<QueryResponse, T>() {
//            @Override
//            public T call(final QueryResponse resp) {
//                LOG.debug("query {}",resp);
//                if (Objects.equals("0", resp.getCode())) {
//                    List<List<Object>> rows = resp.getRowList();
//                    List<String> cols = resp.getColList();
//                    List<Integer> types = resp.getTypeList();
//                    if(rows.size()>0){
//                        return rowMapper.mapRow(new DefaultDBRow(rows.get(0), cols, types));
//                    }else{
//                        return null;
//                    }
//                } else {
//                    LOG.error(resp.getMessage());
//                    throw new RuntimeException(resp.getMessage());
//                }
//            }
//        });
//    }
//
//    @Override
//    public <T> Observable<T> queryForObject(String sql, DBRowMapper<T> rowMapper)  {
//        return this.queryForObject(sql,rowMapper,null);
//    }
//
//    @Override
//    public <T> Observable<T> queryForObject(final String sql,final Class<T> requiredType, Object... args)  {
//        QueryRequest request = new QueryRequest();
//        request.setParameters(convert(args));
//        request.setQuery(sql);
//        request.setProject(_project);
//        request.setDatabase(_database);
//        return _signalClient.interaction().request(request).<QueryResponse>build().map(new Func1<QueryResponse, T>() {
//            @Override
//            public T call(final QueryResponse resp) {
//                LOG.debug("query {}",resp);
//                if (Objects.equals("0", resp.getCode())) {
//                    List<List<Object>> rows = resp.getRowList();
//                    List<String> cols = resp.getColList();
//                    List<Integer> types = resp.getTypeList();
//
//                    if(rows.size()>0){
//                        return new DefaultDBRow(rows.get(0),cols,types).getOnlyObject(requiredType);
//                    }else{
//                        return null;
//                    }
//                } else {
//                    LOG.error(resp.getMessage());
//                    throw new RuntimeException(resp.getMessage());
//                }
//            }
//        });
//    }
//
//    @Override
//    public Observable<DBRow> queryForRowSet(String sql, Object... args){
//        QueryRequest request = new QueryRequest();
//        request.setParameters(convert(args));
//        request.setQuery(sql);
//        request.setProject(_project);
//        request.setDatabase(_database);
//        return _signalClient.interaction().request(request).<QueryResponse>build().map(new Func1<QueryResponse, DBRow>() {
//            @Override
//            public DBRow call(final QueryResponse resp) {
//                LOG.debug("query {}",resp);
//                if(Objects.equals("0", resp.getCode())){
//                    List<List<Object>> rows = resp.getRowList();
//                    List<String> cols = resp.getColList();
//                    List<Integer> types = resp.getTypeList();
//
//                    if(rows.size()>0){
//                        return new DefaultDBRow(rows.get(0),cols,types);
//                    }else{
//                        return null;
//                    }
//                }else{
//                    LOG.error(resp.getMessage());
//                    throw new RuntimeException(resp.getMessage());
//                }
//            }
//        });
//
//    }
//    @Override
//    public Observable<Integer> update(String sql)  {
//        return this.update(sql,null);
//    }
//    @Override
//    public <T> Observable<T> updateAndNeedReturnId(String sql, Object... args)  {
//        UpdateRequest request = new UpdateRequest();
//        request.setParameters(convert(args));
//        request.setQuery(sql);
//        request.setProject(_project);
//        request.setDatabase(_database);
//        request.setNeedReturnId(true);
//        return this.update(request);
//    }
//    @Override
//    public Observable<Integer> update(String sql, Object... args)  {
//        UpdateRequest request = new UpdateRequest();
//        request.setParameters(convert(args));
//        request.setQuery(sql);
//        request.setProject(_project);
//        request.setDatabase(_database);
//        request.setNeedReturnId(false);
//        return this.update(request);
//    }
//
//    private <T> Observable<T> update(UpdateRequest request){
//        return _signalClient.interaction().request(request).<UpdateResponse>build().map(new Func1<UpdateResponse, T>() {
//            @Override
//            public T call(final UpdateResponse resp) {
//                if (Objects.equals("0", resp.getCode())) {
//                    if (resp.getReturnId() != null) {
//                        return (T) resp.getReturnId();
//                    } else {
//                        return (T) resp.getRowCount();
//                    }
//                } else {
//                    LOG.error(resp.getMessage());
//                    throw new RuntimeException(resp.getMessage());
//                }
//            }
//        });
//    }
//
//    @Override
//    public Observable<int[]> batchUpdate(String sql, List<Object[]> batchArgs)  {
//        for(Object[] args:batchArgs){
//            convert(args);
//        }
//        BatchUpdateRequest request = new BatchUpdateRequest();
//        request.setParameters(batchArgs);
//        request.setQuery(sql);
//        request.setProject(_project);
//        request.setDatabase(_database);
//        return _signalClient.interaction().request(request).<BatchUpdateResponse>build().map(new Func1<BatchUpdateResponse, int[]>() {
//            @Override
//            public int[] call(final BatchUpdateResponse resp) {
//                if (Objects.equals("0", resp.getCode())) {
//                    return resp.getRowCount();
//                } else {
//                    LOG.error(resp.getMessage());
//                    throw new RuntimeException(resp.getMessage());
//                }
//            }
//        });
//    }
//    @Override
//    public Observable<int[]> batchUpdate(String[] sqls)  {
//        BatchUpdateRequest request = new BatchUpdateRequest();
//        request.setQuerys(sqls);
//        request.setProject(_project);
//        request.setDatabase(_database);
//        return _signalClient.interaction().request(request).<BatchUpdateResponse>build().map(new Func1<BatchUpdateResponse, int[]>() {
//            @Override
//            public int[] call(final BatchUpdateResponse resp) {
//                if (Objects.equals("0", resp.getCode())) {
//                    return resp.getRowCount();
//                } else {
//                    LOG.error(resp.getMessage());
//                    throw new RuntimeException(resp.getMessage());
//                }
//            }
//        });
//    }
//
//}