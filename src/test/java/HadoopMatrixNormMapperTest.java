import org.apache.hadoop.io.LongWritable;
import org.apache.hadoop.mapreduce.Mapper;
import org.unipi.matrixnorm.HadoopMatrixNorm;
import org.unipi.matrixnorm.MapperKey;
import org.unipi.matrixnorm.MapperValue;

import org.apache.hadoop.io.Text;

import java.io.IOException;

import org.junit.Test;
import org.junit.Before;

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;

import org.mockito.InOrder;

public class HadoopMatrixNormMapperTest {

    private HadoopMatrixNorm.MatrixNormMapper mapper;
    private HadoopMatrixNorm.MatrixNormMapper.Context context;

    @Before
    public void init() throws IOException, InterruptedException {
        mapper = (new HadoopMatrixNorm.MatrixNormMapper());

        context = mock(HadoopMatrixNorm.MatrixNormMapper.Context.class);
    }

    @Test(expected = IllegalArgumentException.class)
    public void wrongInput() throws IOException, InterruptedException {

        mapper.map(null, new Text("1\t2\t1.0"), context);

        InOrder inOrder = inOrder(context);
        inOrder.verify(context, never()).write(
                eq(new MapperKey(0, 0, 0)),
                eq(new MapperValue(0, 0, 1.0))
        );
    }

    @Test
    public void oneElementMatrixTest() throws IOException, InterruptedException {

        mapper.map(null, new Text("1\t1\t1.0"), context);

        InOrder inOrder = inOrder(context);

        inOrder.verify(context).write(
                eq(new MapperKey(0, 0, 0)),
                eq(new MapperValue(0, 0, 1.0))
        );

        inOrder.verify(context).write(
                eq(new MapperKey(0, 0, 1)),
                eq(new MapperValue(0, 0, 1.0))
        );
    }

    @Test
    public void zeroMatrixTest() throws IOException, InterruptedException {

        mapper.map(null, new Text("2\t2\t0.0\t0.0\t0.0\t0.0"), context);

        InOrder inOrder = inOrder(context);

        inOrder.verify(context).write(
                eq(new MapperKey(0, 0, 0)),
                eq(new MapperValue(0, 0, 0.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 0, 1)),
                eq(new MapperValue(0, 0, 0.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 1, 0)),
                eq(new MapperValue(0, 0, 0.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 1, 1)),
                eq(new MapperValue(0, 0, 0.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 0, 0)),
                eq(new MapperValue(0, 1, 0.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 0, 1)),
                eq(new MapperValue(0, 1, 0.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 1, 0)),
                eq(new MapperValue(0, 1, 0.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 1, 1)),
                eq(new MapperValue(0, 1, 0.0))
        );
    }

    @Test
    public void nonZeroMatrixTest() throws IOException, InterruptedException {

        mapper.map(
                null,
                new Text("4\t2\t9.0\t6.0\t0.0\t1.0\t1.0\t0.0\t3.0\t6.0"),
                context);

        InOrder inOrder = inOrder(context);

        inOrder.verify(context).write(
                eq(new MapperKey(0, 0, 0)),
                eq(new MapperValue(0, 0, 9.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 0, 1)),
                eq(new MapperValue(0, 0, 9.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 1, 0)),
                eq(new MapperValue(0, 0, 6.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 1, 1)),
                eq(new MapperValue(0, 0, 6.0))
        );

        inOrder.verify(context).write(
                eq(new MapperKey(0, 0, 0)),
                eq(new MapperValue(0, 1, 0.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 0, 1)),
                eq(new MapperValue(0, 1, 0.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 1, 0)),
                eq(new MapperValue(0, 1, 1.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 1, 1)),
                eq(new MapperValue(0, 1, 1.0))
        );

        inOrder.verify(context).write(
                eq(new MapperKey(0, 0, 0)),
                eq(new MapperValue(0, 2, 1.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 0, 1)),
                eq(new MapperValue(0, 2, 1.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 1, 0)),
                eq(new MapperValue(0, 2, 0.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 1, 1)),
                eq(new MapperValue(0, 2, 0.0))
        );

        inOrder.verify(context).write(
                eq(new MapperKey(0, 0, 0)),
                eq(new MapperValue(0, 3, 3.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 0, 1)),
                eq(new MapperValue(0, 3, 3.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 1, 0)),
                eq(new MapperValue(0, 3, 6.0))
        );
        inOrder.verify(context).write(
                eq(new MapperKey(0, 1, 1)),
                eq(new MapperValue(0, 3, 6.0))
        );
    }

}