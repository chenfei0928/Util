package io.github.chenfei0928.media;

/**
 * Pcm录音转换wav工具类
 *
 * @author ChenFei(chenfei0928 @ gmail.com)
 * @date 2019-11-07 17:39
 * @see <a href="https://www.jianshu.com/p/f7863638acbe">原博文</a>
 */
public class PcmToWavConverter {
    public static final int HEADER_SIZE_WAVE = 44;
    /**
     * 目标大小，单位：字节
     */
    public final int bufferSize;
    private final byte[] header;

    /**
     * @param totalAudioLen 预计的整个音频PCM数据大小，单位：字节
     * @param sampleRate    采样率，例如44100
     * @param channels      声道数 单声道：1或双声道：2
     * @param bitNum        采样位数，8或16
     */
    public PcmToWavConverter(int totalAudioLen, int sampleRate, byte channels, byte bitNum) {
        header = createHeader(totalAudioLen, sampleRate, channels, bitNum);
        bufferSize = HEADER_SIZE_WAVE + totalAudioLen;
    }

    /**
     * @param totalAudioLen 预计的整个音频PCM数据大小，单位：字节
     * @param sampleRate    采样率，例如44100
     * @param channels      声道数 单声道：1或双声道：2
     * @param bitNum        采样位数，8或16
     */
    private static byte[] createHeader(long totalAudioLen, int sampleRate, byte channels, byte bitNum) {
        // 采样字节byte率
        long byteRate = sampleRate * channels * bitNum / 8;
        // 总大小，由于不包括RIFF和WAV，所以是44 - 8 = 36，在加上PCM文件大小
        long totalDataLen = totalAudioLen + 36;
        byte[] header = new byte[HEADER_SIZE_WAVE];
        // RIFF
        header[0] = 'R';
        header[1] = 'I';
        header[2] = 'F';
        header[3] = 'F';
        // 数据大小
        header[4] = (byte) (totalDataLen & 0xff);
        header[5] = (byte) ((totalDataLen >> 8) & 0xff);
        header[6] = (byte) ((totalDataLen >> 16) & 0xff);
        header[7] = (byte) ((totalDataLen >> 24) & 0xff);
        // WAVE
        header[8] = 'W';
        header[9] = 'A';
        header[10] = 'V';
        header[11] = 'E';
        //FMT Chunk
        // 'fmt '
        header[12] = 'f';
        header[13] = 'm';
        header[14] = 't';
        header[15] = ' ';
        // 数据大小
        // 4 bytes: size of 'fmt ' chunk
        header[16] = 16;
        header[17] = 0;
        header[18] = 0;
        header[19] = 0;
        // 编码方式 10H为PCM编码格式
        // format = 1
        header[20] = 1;
        header[21] = 0;
        //通道数
        header[22] = channels;
        header[23] = 0;
        //采样率，每个通道的播放速度
        header[24] = (byte) (sampleRate & 0xff);
        header[25] = (byte) ((sampleRate >> 8) & 0xff);
        header[26] = (byte) ((sampleRate >> 16) & 0xff);
        header[27] = (byte) ((sampleRate >> 24) & 0xff);
        //音频数据传送速率,采样率*通道数*采样深度/8
        header[28] = (byte) (byteRate & 0xff);
        header[29] = (byte) ((byteRate >> 8) & 0xff);
        header[30] = (byte) ((byteRate >> 16) & 0xff);
        header[31] = (byte) ((byteRate >> 24) & 0xff);
        // 确定系统一次要处理多少个这样字节的数据，确定缓冲区，通道数*采样位数
        header[32] = (byte) (channels * 16 / 8);
        header[33] = 0;
        //每个样本的数据位数
        header[34] = 16;
        header[35] = 0;
        // Data chunk
        // data
        header[36] = 'd';
        header[37] = 'a';
        header[38] = 't';
        header[39] = 'a';
        header[40] = (byte) (totalAudioLen & 0xff);
        header[41] = (byte) ((totalAudioLen >> 8) & 0xff);
        header[42] = (byte) ((totalAudioLen >> 16) & 0xff);
        header[43] = (byte) ((totalAudioLen >> 24) & 0xff);
        return header;
    }

    public int converter(short[] pcmData, int size, byte[] target) {
        // 文件头数据
        System.arraycopy(header, 0, target, 0, HEADER_SIZE_WAVE);
        // 将pcm数据输出到target
        for (int i = 0; i < size; i++) {
            int it = pcmData[i];
            // 高8bit内存位
            target[HEADER_SIZE_WAVE + i++] = (byte) ((it >> 8) & 0xff);
            // 低8bit内存位
            target[HEADER_SIZE_WAVE + i++] = (byte) (it & 0xff);
        }
        // 修复数据大小
        fixSize(target, size * 2);
        return HEADER_SIZE_WAVE + size * 2;
    }

    public int converter(byte[] pcmData, int size, byte[] target) {
        // 文件头数据
        System.arraycopy(header, 0, target, 0, HEADER_SIZE_WAVE);
        // 将pcm数据输出到target
        System.arraycopy(pcmData, 0, target, HEADER_SIZE_WAVE, size);
        fixSize(target, size);
        return HEADER_SIZE_WAVE + size;
    }

    private void fixSize(byte[] target, int size) {
        // 总大小，由于不包括RIFF和WAV，所以是44 - 8 = 36，在加上PCM文件大小
        long totalDataLen = size + 36;
        // 数据大小
        target[4] = (byte) (totalDataLen & 0xff);
        target[5] = (byte) ((totalDataLen >> 8) & 0xff);
        target[6] = (byte) ((totalDataLen >> 16) & 0xff);
        target[7] = (byte) ((totalDataLen >> 24) & 0xff);
        // data
        target[40] = (byte) (size & 0xff);
        target[41] = (byte) ((size >> 8) & 0xff);
        target[42] = (byte) ((size >> 16) & 0xff);
        target[43] = (byte) ((size >> 24) & 0xff);
    }
}
