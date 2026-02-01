import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { encodeUserId } from '../lib/base64url';
import { Box, Button, Container, Paper, TextField, Typography } from '@mui/material';

const CreatePage: React.FC = () => {
    const [username, setUsername] = useState('');
    const navigate = useNavigate();

    const handleSubmit = (e: React.FormEvent) => {
        e.preventDefault();
        if (!username.trim()) return;
        
        const userId = encodeUserId(username.trim());
        navigate(`/shop/${userId}`);
    };

    return (
        <Container maxWidth="sm" sx={{ minHeight: '100vh', display: 'flex', alignItems: 'center', justifyContent: 'center' }}>
            <Paper
                elevation={4}
                sx={{
                    p: 5,
                    width: '100%',
                    borderRadius: 4,
                    bgcolor: 'rgba(30, 41, 59, 0.7)',
                    backdropFilter: 'blur(12px)',
                    border: '1px solid rgba(255, 255, 255, 0.1)',
                }}
            >
                <Typography
                    variant="h4"
                    component="h1"
                    align="center"
                    gutterBottom
                    sx={{
                        fontWeight: 700,
                        mb: 4,
                        background: 'linear-gradient(to right, #60a5fa, #c084fc)',
                        WebkitBackgroundClip: 'text',
                        WebkitTextFillColor: 'transparent',
                    }}
                >
                    사용자 이름 입력
                </Typography>
                
                <form onSubmit={handleSubmit}>
                    <Box sx={{ display: 'flex', flexDirection: 'column', gap: 3 }}>
                        <TextField
                            label="마인크래프트 닉네임"
                            variant="outlined"
                            fullWidth
                            value={username}
                            onChange={e => setUsername(e.target.value)}
                            placeholder="예: Steve"
                            sx={{
                                '& .MuiOutlinedInput-root': {
                                    bgcolor: 'rgba(0, 0, 0, 0.2)',
                                },
                            }}
                        />
                        <Button
                            type="submit"
                            variant="contained"
                            size="large"
                            fullWidth
                            sx={{
                                py: 1.5,
                                fontSize: '1.1rem',
                                background: 'linear-gradient(to right, #3b82f6, #8b5cf6)',
                                boxShadow: '0 4px 6px -1px rgba(0, 0, 0, 0.2)',
                            }}
                        >
                            상점으로 이동
                        </Button>
                    </Box>
                </form>
            </Paper>
        </Container>
    );
};

export default CreatePage;
