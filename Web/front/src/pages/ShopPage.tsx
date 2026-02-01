import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { decodeUserId } from '../lib/base64url';
import { Box, Button, Container, Paper, Typography, Alert, Stack } from '@mui/material';

const ShopPage: React.FC = () => {
    const { userId } = useParams<{ userId: string }>();
    const [username, setUsername] = useState<string>('');
    const [error, setError] = useState<string | null>(null);
    const [loading, setLoading] = useState(false);
    const [message, setMessage] = useState<string | null>(null);

    useEffect(() => {
        if (userId) {
            try {
                const decoded = decodeUserId(userId);
                setUsername(decoded);
            } catch (e) {
                setError("잘못된 유저 ID입니다.");
            }
        }
    }, [userId]);

    const handleBuy = async () => {
        if (!username || !userId) return;
        setMessage(null);
        setError(null);
        setLoading(true);
        try {
            const res = await fetch('/api/shop/buy', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ userId: userId })
            });
            const data = await res.json();
            
            if (data.ok) {
                setMessage("구매 성공! 인벤토리에서 월드 접속권을 확인하세요.");
            } else {
                if (data.error === "PLAYER_OFFLINE") {
                    setError("플레이어가 온라인 상태여야 합니다.");
                } else if (data.error === "INSUFFICIENT_DIAMONDS") {
                    setError("다이아몬드가 부족합니다.");
                } else {
                    setError("오류: " + data.error);
                }
            }
        } catch (e) {
            setError("구매 실패. 서버 오류.");
        } finally {
            setLoading(false);
        }
    };

    if (error && !username) return (
        <Container maxWidth="sm" sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Alert severity="error" variant="filled" sx={{ width: '100%' }}>
                오류: {error}
            </Alert>
        </Container>
    );

    return (
        <Container maxWidth="sm" sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Paper
                elevation={4}
                sx={{
                    p: 4,
                    width: '100%',
                    borderRadius: 4,
                    bgcolor: 'rgba(30, 41, 59, 0.7)',
                    backdropFilter: 'blur(12px)',
                    border: '1px solid rgba(255, 255, 255, 0.1)',
                    textAlign: 'center'
                }}
            >
                <Box mb={4}>
                    <Typography
                        variant="h4"
                        component="h1"
                        gutterBottom
                        sx={{
                            fontWeight: 700,
                            background: 'linear-gradient(to right, #60a5fa, #c084fc)',
                            WebkitBackgroundClip: 'text',
                            WebkitTextFillColor: 'transparent',
                        }}
                    >
                        {username}님의 상점
                    </Typography>
                    <Typography variant="body1" color="text.secondary">
                        환영합니다! 필요한 아이템을 구매하세요.
                    </Typography>
                </Box>

                <Paper
                    variant="outlined"
                    sx={{
                        p: 3,
                        mb: 4,
                        bgcolor: 'rgba(0, 0, 0, 0.2)',
                        borderColor: 'rgba(255, 255, 255, 0.05)',
                        textAlign: 'left'
                    }}
                >
                    <Box display="flex" justifyContent="space-between" alignItems="center" mb={1}>
                        <Typography variant="h6" fontWeight="600">
                            월드 접속권
                        </Typography>
                        <Typography variant="h6" color="primary.main" fontWeight="bold">
                            💎 1 다이아몬드
                        </Typography>
                    </Box>
                    <Typography variant="body2" color="text.secondary">
                        다른 플레이어의 개인 월드에 입장할 수 있는 티켓입니다.
                    </Typography>
                </Paper>

                <Stack spacing={2}>
                    {message && (
                        <Alert severity="success" variant="outlined">
                            {message}
                        </Alert>
                    )}
                    
                    {error && (
                        <Alert severity="error" variant="outlined">
                            {error}
                        </Alert>
                    )}

                    <Button
                        variant="contained"
                        size="large"
                        onClick={handleBuy}
                        disabled={loading}
                        sx={{
                            py: 1.5,
                            fontSize: '1.1rem',
                            background: loading ? undefined : 'linear-gradient(to right, #3b82f6, #8b5cf6)',
                            boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.2)',
                        }}
                    >
                        {loading ? '처리 중...' : '구매하기'}
                    </Button>
                </Stack>
            </Paper>
        </Container>
    );
};

export default ShopPage;
